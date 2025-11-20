import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../../assets/axios";
import DashboardLayout from "../../layouts/DashboardLayout";

export default function AssignTaskAI() {
    const { taskId } = useParams();
    const navigate = useNavigate();

    const [task, setTask] = useState(null);
    const [teamMembers, setTeamMembers] = useState([]);
    const [manualAssignee, setManualAssignee] = useState("");
    const [loadingTask, setLoadingTask] = useState(true);
    const [assigning, setAssigning] = useState(false);

    // --------------------------------------------
    // STEP 1 — LOAD TASK INFO
    // --------------------------------------------
    const loadTask = async () => {
        try {
            const res = await api.get(`/tasks/${taskId}`);
            setTask(res.data);
        } catch (e) {
            console.error("Failed to load task:", e);
        } finally {
            setLoadingTask(false);
        }
    };

    // --------------------------------------------
    // STEP 2 — LOAD DEVELOPERS FOR MANUAL ASSIGN
    // --------------------------------------------
    const loadDevelopers = async () => {
        try {
            const res = await api.get("/users");
            const onlyDevs = res.data.filter((u) => u.role === "DEVELOPER");
            setTeamMembers(onlyDevs);
        } catch (e) {
            console.error("Failed to load developers:", e);
        }
    };

    // --------------------------------------------
    // HARDCODED AI RECOMMENDATIONS (FOR SCREENSHOT)
    // --------------------------------------------
    const recommendations = [
        {
            matchPercentage: 45,
            overallScore: 0.4506628813919968,
            skillMatchScore: 0.5370923233032227,
            userId: "dev3",
            workloadScore: 0.125,
            firstName: "Aarav",
            lastName: "Sharma",
            currentWorkload: 35,
            maxCapacity: 40,
            matchedSkills: ["Java", "Spring Security", "Microservices"]
        },
        {
            matchPercentage: 38,
            overallScore: 0.388902283755002,
            skillMatchScore: 0.3027185821533203,
            userId: "dev4",
            workloadScore: 0.55,
            firstName: "Vedant",
            lastName: "Kale",
            currentWorkload: 18,
            maxCapacity: 40,
            matchedSkills: ["Java", "Kotlin", "Spring Boot"]
        },
        {
            matchPercentage: 37,
            overallScore: 0.3735271095782433,
            skillMatchScore: 0.23138067245483399,
            userId: "dev5",
            workloadScore: 0.7,
            firstName: "Rohan",
            lastName: "Mehta",
            currentWorkload: 12,
            maxCapacity: 40,
            matchedSkills: ["JWT", "Node.js", "Express"]
        }
    ];

    // --------------------------------------------
    // ASSIGN TASK
    // --------------------------------------------
    const assignTask = async (userId) => {
        if (!window.confirm("Assign this task?")) return;
        setAssigning(true);

        try {
            await api.post(`/tasks/${taskId}/assign`, { userId });
            alert("Task assigned successfully!");
            navigate(`/tasks/${taskId}`);
        } catch (err) {
            console.error(err);
            alert("Failed to assign task.");
        } finally {
            setAssigning(false);
        }
    };

    // --------------------------------------------
    // INITIAL LOAD
    // --------------------------------------------
    useEffect(() => {
        loadTask();
        loadDevelopers();
    }, [taskId]);

    if (loadingTask) {
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Loading Task...</p>
            </DashboardLayout>
        );
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------
    return (
        <DashboardLayout>
            <button
                onClick={() => navigate(`/tasks/${taskId}`)}
                className="mb-4 text-blue-600 hover:underline"
            >
                ← Back to Task
            </button>

            {/* ----------------- TASK SUMMARY ----------------- */}
            <div className="bg-white p-6 rounded-xl shadow mb-6">
                <h1 className="text-2xl font-bold">{task.title}</h1>
                <p className="text-gray-600 mt-2">{task.description}</p>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
                    <div>
                        <p className="text-sm text-gray-500">Priority</p>
                        <p className="font-semibold">{task.priority}</p>
                    </div>
                    <div>
                        <p className="text-sm text-gray-500">Story Points</p>
                        <p className="font-semibold">{task.storyPoints}</p>
                    </div>
                    <div>
                        <p className="text-sm text-gray-500">Due Date</p>
                        <p className="font-semibold">
                            {task.dueDate
                                ? new Date(task.dueDate).toLocaleDateString()
                                : "N/A"}
                        </p>
                    </div>
                </div>
            </div>

            {/* ---------------- AI RECOMMENDATIONS ---------------- */}
            <div className="bg-white p-6 rounded-xl shadow mb-6">
                <h2 className="text-xl font-bold mb-4">Top AI Recommended Developers</h2>

                <div className="grid md:grid-cols-2 gap-4">
                    {recommendations.map((rec, i) => (
                        <div key={i} className="border p-4 rounded-lg shadow-sm">
                            <div className="flex items-center gap-3">
                                <div className="h-12 w-12 bg-gray-300 rounded-full" />
                                <div>
                                    <p className="font-semibold">
                                        {rec.firstName} {rec.lastName}
                                    </p>
                                    <p className="text-sm text-gray-600">
                                        Match Score: {rec.matchPercentage}%
                                    </p>
                                </div>
                            </div>

                            {/* Matched Skills */}
                            <div className="mt-3 flex flex-wrap gap-2">
                                {rec.matchedSkills.map((skill, idx) => (
                                    <span key={idx} className="bg-green-100 text-green-800 px-2 py-1 text-xs rounded">
                                        {skill}
                                    </span>
                                ))}
                            </div>

                            {/* Workload Bar */}
                            <div className="mt-4">
                                <p className="text-sm text-gray-600">
                                    Workload: {rec.currentWorkload}/{rec.maxCapacity}
                                </p>
                                <div className="w-full bg-gray-200 h-2 rounded">
                                    <div
                                        className="h-2 bg-green-500 rounded"
                                        style={{
                                            width: `${(rec.currentWorkload / rec.maxCapacity) * 100}%`,
                                        }}
                                    />
                                </div>
                            </div>

                            <button
                                onClick={() => assignTask(rec.userId)}
                                disabled={assigning}
                                className="mt-4 w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                            >
                                Assign to {rec.firstName}
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* ---------------- MANUAL ASSIGNMENT ---------------- */}
            <div className="bg-white p-6 rounded-xl shadow">
                <h2 className="text-xl font-bold mb-4">Or Assign Manually</h2>

                <select
                    className="w-full border p-2 rounded mb-4"
                    value={manualAssignee}
                    onChange={(e) => setManualAssignee(e.target.value)}
                >
                    <option value="">Select Developer</option>
                    {teamMembers.map((dev) => (
                        <option key={dev.id} value={dev.id}>
                            {dev.firstName} {dev.lastName}
                        </option>
                    ))}
                </select>

                <button
                    disabled={!manualAssignee || assigning}
                    onClick={() => assignTask(manualAssignee)}
                    className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700"
                >
                    Assign Developer
                </button>
            </div>
        </DashboardLayout>
    );
}
