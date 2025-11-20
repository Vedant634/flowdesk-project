import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import DashboardLayout from "../../layouts/DashboardLayout";
import api from "../../assets/axios";

export default function TeamWorkloadPage() {
    const { teamId } = useParams();
    const navigate = useNavigate();

    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [suggestions, setSuggestions] = useState([]);
    const [showTasksModal, setShowTasksModal] = useState(null); // stores user object

    useEffect(() => {
        loadData();
    }, [teamId]);

    const loadData = async () => {
        setLoading(true);
        try {
            const res = await api.get(`/teams/${teamId}/workload`);
            setData(res.data);

            generateSuggestions(res.data);
        } finally {
            setLoading(false);
        }
    };

    /** ---------------------------
     *  AI-LIKE SUGGESTION GENERATION
     *  --------------------------*/
    const generateSuggestions = (workload) => {
        if (!workload?.members) return;

        const overloaded = workload.members.filter(m => m.utilizationPercentage > 100);
        const underloaded = workload.members.filter(m => m.utilizationPercentage < 70);

        const s = [];

        overloaded.forEach(ov => {
            const sortedTasks = [...ov.activeTasks].sort((a, b) => a.storyPoints - b.storyPoints);

            sortedTasks.forEach(task => {
                const target = underloaded[0];
                if (!target) return;

                s.push({
                    task,
                    from: ov.user,
                    to: target.user
                });
            });
        });

        setSuggestions(s);
    };

    /** ---------------------------
     *  APPLY REASSIGNMENT
     *  --------------------------*/
    const applySuggestion = async (taskId, newUserId) => {
        if (!window.confirm("Reassign this task?")) return;

        try {
            await api.post(`/tasks/${taskId}/assign`, { userId: newUserId });
            alert("Task reassigned successfully.");
            loadData();
        } catch (err) {
            console.error(err);
            alert("Failed to reassign task.");
        }
    };

    if (loading) {
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Loading workload...</p>
            </DashboardLayout>
        );
    }

    const teamName = data?.teamName || "Team";

    return (
        <DashboardLayout>
            {/* Header */}
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-3xl font-bold">Team Workload: {teamName}</h1>
                    <p className="text-gray-600">Team performance & task load overview</p>
                </div>

                <div className="flex gap-3">
                    <button
                        onClick={() => navigate(`/teams/${teamId}`)}
                        className="bg-gray-200 px-4 py-2 rounded hover:bg-gray-300"
                    >
                        ← Back
                    </button>
                    <button
                        onClick={loadData}
                        className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                    >
                        Refresh
                    </button>
                </div>
            </div>

            {/* Overall Team Status */}
            <div className="bg-white p-6 rounded-xl shadow mb-6">
                <h2 className="text-xl font-bold mb-4">Overall Team Health</h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="text-center">
                        <p className="text-gray-500 text-sm">Average Utilization</p>
                        <p className="text-3xl font-bold">{data.averageUtilization.toFixed(1)}%</p>
                    </div>
                    <div className="text-center">
                        <p className="text-gray-500 text-sm">Status</p>
                        <p
                            className={`text-3xl font-bold ${
                                data.isBalanced ? "text-green-600" : "text-red-600"
                            }`}
                        >
                            {data.isBalanced ? "Balanced" : "Imbalanced"}
                        </p>
                    </div>
                    <div className="text-center">
                        <p className="text-gray-500 text-sm">Active Tasks</p>
                        <p className="text-3xl font-bold">
                            {data.members.reduce((sum, m) => sum + m.activeTasks.length, 0)}
                        </p>
                    </div>
                </div>
            </div>

            {/* Team Members Grid */}
            <h2 className="text-xl font-bold mb-4">Members Workload</h2>
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
                {data.members.map((m) => (
                    <div key={m.user.id} className="bg-white p-5 shadow rounded-xl relative">
                        {/* Avatar */}
                        <div className="h-14 w-14 bg-gray-300 rounded-full mb-3"></div>

                        <p className="font-semibold text-lg">
                            {m.user.firstName} {m.user.lastName}
                        </p>

                        {/* Utilization color */}
                        <p
                            className={`font-bold mt-2 ${
                                m.utilizationPercentage < 70
                                    ? "text-green-600"
                                    : m.utilizationPercentage <= 90
                                    ? "text-yellow-600"
                                    : "text-red-600"
                            }`}
                        >
                            {m.utilizationPercentage}% Utilized
                        </p>

                        {/* Workload Bar */}
                        <div className="w-full bg-gray-200 h-2 rounded mt-3">
                            <div
                                className="h-2 rounded bg-blue-600"
                                style={{
                                    width: `${(m.currentWorkload / m.maxCapacity) * 100}%`,
                                }}
                            ></div>
                        </div>

                        <p className="text-sm text-gray-600 mt-2">
                            {m.currentWorkload}/{m.maxCapacity} points
                        </p>

                        <p className="text-gray-600 text-sm mt-2">
                            Active Tasks: {m.activeTasks.length}
                        </p>

                        <button
                            onClick={() => setShowTasksModal(m)}
                            className="mt-4 bg-blue-600 text-white w-full py-2 rounded hover:bg-blue-700"
                        >
                            View Tasks
                        </button>
                    </div>
                ))}
            </div>

            {/* Suggestions */}
            {!data.isBalanced && (
                <div className="bg-red-50 border border-red-300 p-6 rounded-xl shadow">
                    <h2 className="text-xl font-bold text-red-700 mb-4">
                        ⚠ Workload Imbalance Detected
                    </h2>

                    {suggestions.map((s, i) => (
                        <div key={i} className="border p-4 rounded-lg mb-3 bg-white">
                            <p className="font-semibold">
                                Move <strong>{s.task.title}</strong> ({s.task.storyPoints} pts)
                            </p>
                            <p className="text-sm">
                                from <strong>{s.from.firstName}</strong> →{" "}
                                <strong>{s.to.firstName}</strong>
                            </p>

                            <button
                                onClick={() => applySuggestion(s.task.id, s.to.id)}
                                className="mt-3 bg-purple-600 text-white px-3 py-2 rounded hover:bg-purple-700"
                            >
                                Apply Reassignment
                            </button>
                        </div>
                    ))}
                </div>
            )}

            {/* Modal for Viewing Tasks */}
            {showTasksModal && (
                <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center">
                    <div className="bg-white p-6 rounded-xl w-96 shadow-lg">
                        <h2 className="text-xl font-bold mb-4">
                            Tasks of {showTasksModal.user.firstName}
                        </h2>

                        <div className="space-y-3 max-h-64 overflow-y-auto">
                            {showTasksModal.activeTasks.map((t) => (
                                <div key={t.id} className="border p-3 rounded-lg">
                                    <p className="font-semibold">{t.title}</p>
                                    <p className="text-sm text-gray-600">
                                        {t.storyPoints} pts
                                    </p>
                                </div>
                            ))}
                        </div>

                        <button
                            onClick={() => setShowTasksModal(null)}
                            className="mt-4 w-full bg-gray-300 py-2 rounded"
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
