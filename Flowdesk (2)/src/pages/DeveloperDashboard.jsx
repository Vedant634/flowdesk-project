import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import StatsCard from "../components/StatsCard";
import { CheckCircle, AlertCircle, Clock } from "lucide-react";

export default function DeveloperDashboard() {
    const [data, setData] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);

    const navigate = useNavigate();

    // ✅ Correct way to load current user
    const currentUser = JSON.parse(localStorage.getItem("currentUser"));
    const userId = currentUser?.id;

    useEffect(() => {
        if (!userId) {
            console.error("User ID is missing!");
            setLoading(false);
            return;
        }
        loadData();
    }, [userId]);

    const loadData = async () => {
        try {
            const [dashRes, tasksRes] = await Promise.all([
                api.get("/dashboard/developer"),
                api.get(`/tasks/users/${userId}/tasks`),
            ]);

            setData(dashRes.data);
            setTasks(tasksRes.data);
        } catch (err) {
            console.error("Failed to load data", err);
        } finally {
            setLoading(false);
        }
    };

    if (loading)
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Loading dashboard...</p>
            </DashboardLayout>
        );

    if (!data)
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Failed to load data</p>
            </DashboardLayout>
        );

    // Organizing tasks by status
    const tasksByStatus = {
        TODO: tasks.filter((t) => t.status === "TODO"),
        IN_PROGRESS: tasks.filter((t) => t.status === "IN_PROGRESS"),
        IN_REVIEW: tasks.filter((t) => t.status === "IN_REVIEW"),
        DONE: tasks.filter((t) => t.status === "DONE"),
    };

    return (
        <DashboardLayout>
            <h1 className="text-3xl font-bold mb-8">My Dashboard</h1>

            {/* Top Stats */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <StatsCard title="My Tasks" value={data.myTasksCount} color="#3B82F6" />
                <StatsCard title="Current Workload" value={`${data.currentWorkload || 0}%`} color="#10B981" />
                <StatsCard title="Completed This Week" value={data.completedThisWeek} color="#F59E0B" />
                <StatsCard title="Upcoming Deadlines" value={data.upcomingDeadlines?.length || 0} color="#EF4444" />
            </div>

            {/* Task Counters */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-10">
                {["TODO", "IN_PROGRESS", "IN_REVIEW", "DONE"].map((status) => (
                    <div key={status} className="bg-white rounded-lg shadow p-4">
                        <div className="flex items-center gap-2 mb-4">
                            {status === "DONE" && <CheckCircle size={20} className="text-green-600" />}
                            {status === "IN_REVIEW" && <AlertCircle size={20} className="text-yellow-600" />}
                            {(status === "TODO" || status === "IN_PROGRESS") && (
                                <Clock size={20} className="text-blue-600" />
                            )}
                            <h3 className="font-bold">{status.replace(/_/g, " ")}</h3>
                        </div>
                        <p className="text-2xl font-bold">{tasksByStatus[status].length}</p>
                    </div>
                ))}
            </div>

            {/* TODO Tasks Section */}
            <div className="bg-white rounded-lg shadow p-6 mb-8">
                <h2 className="text-xl font-bold mb-4">My TODO Tasks</h2>
                {tasksByStatus.TODO.length === 0 ? (
                    <p className="text-gray-500">No TODO tasks assigned.</p>
                ) : (
                    <div className="space-y-3">
                        {tasksByStatus.TODO.map((task) => (
                            <div
                                key={task.id}
                                onClick={() => navigate(`/tasks/${task.id}`)}
                                className="p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                            >
                                <div className="flex items-start justify-between">
                                    <div>
                                        <h4 className="font-semibold">{task.title}</h4>
                                        <p className="text-sm text-gray-600">{task.description}</p>
                                    </div>
                                    <span className="px-3 py-1 bg-blue-100 text-blue-800 text-xs rounded-full font-semibold">
                                        {task.priority}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Active Tasks */}
            <div className="bg-white rounded-lg shadow p-6 mb-8">
                <h2 className="text-xl font-bold mb-4">My Active Tasks (In Progress)</h2>
                {tasksByStatus.IN_PROGRESS.length === 0 ? (
                    <p className="text-gray-500">No active tasks.</p>
                ) : (
                    <div className="space-y-3">
                        {tasksByStatus.IN_PROGRESS.map((task) => (
                            <div
                                key={task.id}
                                onClick={() => navigate(`/tasks/${task.id}`)}
                                className="p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                            >
                                <div className="flex items-start justify-between">
                                    <div>
                                        <h4 className="font-semibold">{task.title}</h4>
                                        <p className="text-sm text-gray-600">{task.description}</p>
                                    </div>
                                    <span className="px-3 py-1 bg-green-100 text-green-800 text-xs rounded-full font-semibold">
                                        In Progress
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Tasks In Review */}
            <div className="bg-white rounded-lg shadow p-6 mb-8">
                <h2 className="text-xl font-bold mb-4">Tasks In Review</h2>
                {tasksByStatus.IN_REVIEW.length === 0 ? (
                    <p className="text-gray-500">No tasks are currently in review.</p>
                ) : (
                    <div className="space-y-3">
                        {tasksByStatus.IN_REVIEW.map((task) => (
                            <div
                                key={task.id}
                                onClick={() => navigate(`/tasks/${task.id}`)}
                                className="p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                            >
                                <div className="flex items-start justify-between">
                                    <div>
                                        <h4 className="font-semibold">{task.title}</h4>
                                        <p className="text-sm text-gray-600">{task.description}</p>
                                    </div>
                                    <span className="px-3 py-1 bg-yellow-100 text-yellow-800 text-xs rounded-full font-semibold">
                                        In Review
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </DashboardLayout>
    );
}
