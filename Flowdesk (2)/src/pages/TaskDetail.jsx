import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import { ArrowLeft, Send } from "lucide-react";
import AssignDeveloperModal from "../components/AssignDeveloperModal";
import SubmitForReviewModal from "../components/SubmitForReviewModal";
import ApproveTaskModal from "../components/ApproveTaskModal";

export default function TaskDetail() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [task, setTask] = useState(null);
    const [subtasks, setSubtasks] = useState([]);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState("");
    const [loading, setLoading] = useState(true);

    const [currentUser, setCurrentUser] = useState(null);

    const [showAssignModal, setShowAssignModal] = useState(false);
    const [showReviewModal, setShowReviewModal] = useState(false);
    const [showApproveModal, setShowApproveModal] = useState(false);

    const [newSubtaskTitle, setNewSubtaskTitle] = useState("");
    const [subtaskLoading, setSubtaskLoading] = useState(false);

    // --- HARD-CODED RISK PREDICTION ---
    const [riskPrediction, setRiskPrediction] = useState({
        riskLevel: "HIGH",
        riskScore: 0.81,
        willMissDeadline: true,
        lastUpdated: "2025-11-20T10:00:00Z",
        probabilities: {
            LOW: 0.10,
            MEDIUM: 0.09,
            HIGH: 0.81
        }
    });

    useEffect(() => {
        loadTaskData();
        loadUser();
    }, [id]);

    const loadUser = async () => {
        try {
            const res = await api.get("/auth/me");
            setCurrentUser(res.data);
        } catch (err) {
            console.error("Failed to load user", err);
        }
    };

    const loadTaskData = async () => {
        try {
            const [taskRes, subtasksRes, commentsRes] = await Promise.all([
                api.get(`/tasks/${id}`),
                api.get(`/subtasks/tasks/${id}/subtasks`).catch(() => ({ data: [] })),
                api.get(`/comments/tasks/${id}/comments`).catch(() => ({ data: [] })),
            ]);

            setTask(taskRes.data);
            setSubtasks(subtasksRes.data);
            setComments(commentsRes.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddComment = async () => {
        if (!newComment.trim()) return;
        try {
            await api.post("/comments", { taskId: id, content: newComment });
            setNewComment("");
            loadTaskData();
        } catch (err) {
            console.error(err);
        }
    };

    const handleStatusChange = async (newStatus) => {
        try {
            await api.patch(`/tasks/${id}/status`, { status: newStatus });
            loadTaskData();
        } catch (err) {
            console.error(err);
        }
    };

    const handleAddSubtask = async () => {
        if (!newSubtaskTitle.trim()) return;

        try {
            setSubtaskLoading(true);
            await api.post("/subtasks", { taskId: id, title: newSubtaskTitle.trim() });

            setNewSubtaskTitle("");
            loadTaskData();
        } catch (err) {
            console.error("Failed to create subtask", err);
            alert("Error creating subtask");
        } finally {
            setSubtaskLoading(false);
        }
    };

    const handleToggleSubtask = async (subtaskId) => {
        try {
            await api.patch(`/subtasks/${subtaskId}/toggle`);
            loadTaskData();
        } catch (err) {
            console.error("Error toggling subtask", err);
        }
    };

    const handleDeleteSubtask = async (subtaskId) => {
        if (!window.confirm("Delete this subtask?")) return;
        try {
            await api.delete(`/subtasks/${subtaskId}`);
            loadTaskData();
        } catch (err) {
            console.error("Error deleting subtask", err);
        }
    };

    const handleRefreshRisk = () => {
        setRiskPrediction((prev) => ({
            ...prev,
            lastUpdated: new Date().toISOString()
        }));
    };

    const getRiskColor = (level) => {
        switch (level) {
            case "LOW": return "bg-green-100 text-green-800";
            case "MEDIUM": return "bg-yellow-100 text-yellow-800";
            case "HIGH": return "bg-red-100 text-red-800";
            default: return "bg-gray-100 text-gray-800";
        }
    };

    const getDueDateStyle = (dueDateStr) => {
        if (!dueDateStr) return "text-gray-800";
        const today = new Date();
        const due = new Date(dueDateStr);
        const diff = (due - today) / (1000 * 60 * 60 * 24);
        if (diff < 0) return "text-red-600 font-semibold";
        if (diff <= 2) return "text-orange-600 font-semibold";
        return "text-gray-800";
    };

    if (loading) {
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Loading...</p>
            </DashboardLayout>
        );
    }

    if (!task) {
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Task not found</p>
            </DashboardLayout>
        );
    }

    const completedSubtasks = subtasks.filter((s) => s.isCompleted).length;

    return (
        <DashboardLayout>
            {/* BACK BUTTON */}
            <button
                onClick={() => {
                    if (task.projectId) navigate(`/projects/${task.projectId}`);
                    else navigate("/");
                }}
                className="flex items-center gap-2 text-blue-600 hover:text-blue-800 mb-6"
            >
                <ArrowLeft size={20} />
                Back
            </button>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* LEFT */}
                <div className="lg:col-span-2 space-y-6">

                    {/* TASK HEADER */}
                    <div className="bg-white rounded-lg shadow p-6">
                        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
                            <div>
                                <h1 className="text-3xl font-bold mb-1">{task.title}</h1>
                                {task.projectId && (
                                    <button
                                        onClick={() => navigate(`/projects/${task.projectId}`)}
                                        className="text-sm text-blue-600 hover:underline"
                                    >
                                        View Project →
                                    </button>
                                )}
                            </div>

                            <div className="flex flex-wrap gap-2">
                                <span className="px-3 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
                                    Status: {task.status}
                                </span>
                                <span className="px-3 py-1 text-xs font-semibold rounded-full bg-purple-100 text-purple-800">
                                    Priority: {task.priority}
                                </span>
                                <span className={`px-3 py-1 text-xs font-semibold rounded-full ${getRiskColor(riskPrediction.riskLevel)}`}>
                                    Risk: {riskPrediction.riskLevel}
                                </span>
                            </div>
                        </div>

                        {/* TASK DESCRIPTION */}
                        <p className="text-gray-700 mb-6">{task.description}</p>

                        {/* GRID INFO */}
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Story Points</p>
                                <p className="text-lg font-semibold">{task.storyPoints}</p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Estimated Hours</p>
                                <p className="text-lg font-semibold">{task.estimatedHours || 0}h</p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Actual Hours</p>
                                <p className="text-lg font-semibold">{task.actualHoursLogged || 0}h</p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Created By</p>
                                <p className="text-lg font-semibold">
                                    {task.createdBy?.firstName} {task.createdBy?.lastName}
                                </p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Assigned To</p>
                                <p className="text-lg font-semibold">
                                    {task.assignedTo
                                        ? `${task.assignedTo.firstName} ${task.assignedTo.lastName || ""}`
                                        : "Unassigned"}
                                </p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Due Date</p>
                                <p className={`text-lg font-semibold ${getDueDateStyle(task.dueDate)}`}>
                                    {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : "N/A"}
                                </p>
                            </div>
                        </div>

                        {/* Pull Request */}
                        {task.pullRequestUrl && (
                            <div className="bg-gray-50 border rounded-lg p-3 mb-4">
                                <p className="text-gray-600 text-sm mb-1">Pull Request</p>
                                <a
                                    href={task.pullRequestUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="text-blue-600 text-sm break-all hover:underline"
                                >
                                    {task.pullRequestUrl}
                                </a>
                            </div>
                        )}
                    </div>

                    {/* RISK CARD */}
                    <div className="bg-white rounded-lg shadow p-6">
                        <div className="flex items-center justify-between mb-3">
                            <h2 className="text-xl font-bold">Risk Prediction</h2>
                            <button
                                onClick={handleRefreshRisk}
                                className="text-sm bg-blue-600 text-white px-3 py-1 rounded"
                            >
                                Refresh Risk Prediction
                            </button>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Risk Score</p>
                                <p className="text-2xl font-bold">
                                    {Math.round(riskPrediction.riskScore * 100)}
                                </p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Risk Level</p>
                                <span className={`px-3 py-1 rounded-full text-sm font-semibold ${getRiskColor(riskPrediction.riskLevel)}`}>
                                    {riskPrediction.riskLevel}
                                </span>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Will Miss Deadline</p>
                                <p className="text-lg font-semibold">
                                    {riskPrediction.willMissDeadline ? "Yes" : "No"}
                                </p>
                            </div>
                            <div>
                                <p className="text-gray-500 text-xs uppercase">Last Updated</p>
                                <p className="text-sm">{new Date(riskPrediction.lastUpdated).toLocaleString()}</p>
                            </div>
                        </div>

                        <p className="text-xs text-gray-500">
                            Probabilities: LOW {Math.round(riskPrediction.probabilities.LOW * 100)}% · MEDIUM{" "}
                            {Math.round(riskPrediction.probabilities.MEDIUM * 100)}% · HIGH{" "}
                            {Math.round(riskPrediction.probabilities.HIGH * 100)}%
                        </p>
                    </div>

                    {/* SUBTASKS */}
                    <div className="bg-white rounded-lg shadow p-6">
                        <h2 className="text-xl font-bold mb-4">
                            Subtasks ({completedSubtasks}/{subtasks.length})
                        </h2>

                        {/* Add Subtask */}
                        {currentUser?.role === "DEVELOPER" &&
                            currentUser?.id === task.assignedTo?.id && (
                                <div className="flex gap-2 mb-4">
                                    <input
                                        type="text"
                                        value={newSubtaskTitle}
                                        onChange={(e) => setNewSubtaskTitle(e.target.value)}
                                        placeholder="Add a new subtask..."
                                        className="flex-1 border rounded-lg px-3 py-2"
                                    />
                                    <button
                                        onClick={handleAddSubtask}
                                        disabled={subtaskLoading || !newSubtaskTitle.trim()}
                                        className="bg-blue-600 text-white px-4 py-2 rounded-lg"
                                    >
                                        {subtaskLoading ? "Adding..." : "Add"}
                                    </button>
                                </div>
                            )}

                        {subtasks.length === 0 ? (
                            <p className="text-gray-500 text-sm">No subtasks yet.</p>
                        ) : (
                            <div className="space-y-2">
                                {subtasks.map((subtask) => (
                                    <div
                                        key={subtask.id}
                                        className="flex items-center justify-between p-3 border rounded-lg bg-gray-50"
                                    >
                                        <div className="flex items-center gap-3">
                                            <input
                                                type="checkbox"
                                                checked={subtask.isCompleted}
                                                onChange={() => handleToggleSubtask(subtask.id)}
                                                className="w-5 h-5"
                                            />
                                            <div>
                                                <p className={subtask.isCompleted ? "line-through text-gray-500" : "text-gray-800"}>
                                                    {subtask.title}
                                                </p>
                                                {subtask.completedAt && (
                                                    <p className="text-xs text-gray-500">
                                                        Completed at{" "}
                                                        {new Date(subtask.completedAt).toLocaleString()}
                                                    </p>
                                                )}
                                            </div>
                                        </div>

                                        {currentUser?.role === "DEVELOPER" &&
                                            currentUser?.id === task.assignedTo?.id && (
                                                <button
                                                    onClick={() => handleDeleteSubtask(subtask.id)}
                                                    className="text-xs text-red-500"
                                                >
                                                    Delete
                                                </button>
                                            )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* COMMENTS */}
                    <div className="bg-white rounded-lg shadow p-6">
                        <h2 className="text-xl font-bold mb-4">Comments</h2>

                        <div className="space-y-4 mb-6">
                            {comments.length === 0 && (
                                <p className="text-gray-500 text-sm">No comments yet.</p>
                            )}

                            {comments.map((c) => (
                                <div key={c.id} className="border-l-4 border-blue-500 pl-4">
                                    <p className="font-semibold text-sm">
                                        {c.user?.firstName} {c.user?.lastName}
                                    </p>
                                    <p className="text-gray-600 text-xs">
                                        {new Date(c.createdAt).toLocaleString()}
                                    </p>
                                    <p className="mt-2">{c.content}</p>
                                </div>
                            ))}
                        </div>

                        <div className="flex gap-2">
                            <input
                                type="text"
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                                placeholder="Add a comment..."
                                className="flex-1 border rounded-lg px-4 py-2"
                            />
                            <button
                                onClick={handleAddComment}
                                className="bg-blue-600 text-white px-4 rounded-lg"
                            >
                                <Send size={20} />
                            </button>
                        </div>
                    </div>
                </div>

                {/* RIGHT SIDEBAR */}
                <div>
                    <div className="bg-white rounded-lg shadow p-6 sticky top-24">
                        <h3 className="text-lg font-bold mb-4">Task Controls</h3>

                        {/* STATUS DROPDOWN */}
                        <div className="mb-4">
                            <p className="text-gray-600 text-sm mb-1">Status</p>
                            <select
                                value={task.status}
                                onChange={(e) => handleStatusChange(e.target.value)}
                                className="w-full text-lg font-semibold border rounded px-2 py-1"
                                disabled={
                                    !(
                                        currentUser?.role === "MANAGER" ||
                                        currentUser?.id === task.assignedTo?.id
                                    )
                                }
                            >
                                <option>TODO</option>
                                <option>IN_PROGRESS</option>
                                <option>IN_REVIEW</option>
                                <option>DONE</option>
                            </select>
                        </div>

                        {/* ACTION BUTTONS */}
                        <div className="space-y-3 mb-6">
                            {currentUser?.role === "DEVELOPER" &&
                                currentUser?.id === task.assignedTo?.id &&
                                task.status === "TODO" && (
                                    <button
                                        onClick={() => handleStatusChange("IN_PROGRESS")}
                                        className="w-full bg-blue-600 text-white py-2 rounded"
                                    >
                                        Start Working
                                    </button>
                                )}

                            {currentUser?.role === "DEVELOPER" &&
                                currentUser?.id === task.assignedTo?.id &&
                                task.status === "IN_PROGRESS" && (
                                    <button
                                        onClick={() => setShowReviewModal(true)}
                                        className="w-full bg-purple-600 text-white py-2 rounded"
                                    >
                                        Submit For Review
                                    </button>
                                )}

                            {currentUser?.role === "MANAGER" &&
                                task.status === "IN_REVIEW" && (
                                    <button
                                        onClick={() => setShowApproveModal(true)}
                                        className="w-full bg-green-600 text-white py-2 rounded"
                                    >
                                        Approve Task
                                    </button>
                                )}
                        </div>

                        {/* DETAILS */}
                        <div className="space-y-4">
                            <div>
                                <p className="text-gray-600 text-sm">Assigned To</p>
                                <p className="font-semibold">
                                    {task.assignedTo
                                        ? `${task.assignedTo.firstName} ${task.assignedTo.lastName || ""}`
                                        : "Unassigned"}
                                </p>
                            </div>

                            {/* Manager assign button */}
                            {currentUser?.role === "MANAGER" && (
                                <button
                                    onClick={() => setShowAssignModal(true)}
                                    className="w-full bg-blue-600 text-white py-2 rounded"
                                >
                                    Assign Developer
                                </button>
                            )}

                            <div>
                                <p className="text-gray-600 text-sm">Created By</p>
                                <p className="font-semibold">
                                    {task.createdBy?.firstName} {task.createdBy?.lastName}
                                </p>
                            </div>

                            <div>
                                <p className="text-gray-600 text-sm">Project ID</p>
                                <p className="font-mono text-xs bg-gray-100 px-2 py-1 rounded">
                                    {task.projectId}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* MODALS */}
            {showAssignModal && (
                <AssignDeveloperModal
                    taskId={id}
                    onClose={() => setShowAssignModal(false)}
                    onAssigned={loadTaskData}
                />
            )}

            {showReviewModal && (
                <SubmitForReviewModal
                    taskId={id}
                    onClose={() => setShowReviewModal(false)}
                    onSubmitted={loadTaskData}
                />
            )}

            {showApproveModal && (
                <ApproveTaskModal
                    taskId={id}
                    onClose={() => setShowApproveModal(false)}
                    onApproved={loadTaskData}
                />
            )}
        </DashboardLayout>
    );
}
