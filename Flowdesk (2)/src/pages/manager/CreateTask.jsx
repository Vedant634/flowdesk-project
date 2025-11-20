import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../../assets/axios"; 
import DashboardLayout from "../../layouts/DashboardLayout";

export default function CreateTaskPage() {
    const navigate = useNavigate();
    const { projectId } = useParams();

    const [loading, setLoading] = useState(false);
    const [form, setForm] = useState({
        title: "",
        description: "",
        priority: "MEDIUM",
        storyPoints: "",
        estimatedHours: "",
        dueDate: "",
        assignedToUserId: null
    });

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const validate = () => {
        if (form.title.length < 5) return "Title must be at least 5 characters";
        if (!form.description) return "Description required";
        if (form.storyPoints < 1 || form.storyPoints > 20) return "Story Points must be 1–20";
        if (form.estimatedHours < 1 || form.estimatedHours > 200) return "Estimated Hours must be 1–200";
        if (!form.dueDate) return "Due date is required";
        return null;
    };

    const createTask = async (goToAI) => {
        const error = validate();
        if (error) {
            alert(error);
            return;
        }

        setLoading(true);

        try {
            const body = {
                projectId: projectId,
                title: form.title,
                description: form.description,
                priority: form.priority,
                storyPoints: Number(form.storyPoints),
                estimatedHours: Number(form.estimatedHours),
                dueDate: form.dueDate,
                assignedToUserId: form.assignedToUserId || null
            };

            const res = await api.post("/tasks", body);
            const taskId = res.data.id;

            if (goToAI) {
                navigate(`/tasks/${taskId}/assign`);
            } else {
                navigate(`/tasks/${taskId}`);
            }

        } catch (err) {
            console.error("Error creating task:", err.response?.data || err.message);
            alert("ERROR: " + JSON.stringify(err.response?.data));
        } finally {
            setLoading(false);
        }
    };

    return (
        <DashboardLayout>
            <div className="max-w-xl mx-auto mt-10 p-6 bg-white rounded-xl shadow">
                <h2 className="text-2xl font-semibold mb-4">Create New Task</h2>

                <form className="space-y-4">

                    <p className="bg-gray-100 p-2 rounded text-sm">
                        <strong>Project ID:</strong> {projectId}
                    </p>

                    <div>
                        <label className="block mb-1">Title</label>
                        <input
                            type="text"
                            name="title"
                            value={form.title}
                            onChange={handleChange}
                            className="w-full border px-3 py-2 rounded"
                            required
                        />
                    </div>

                    <div>
                        <label className="block mb-1">Description</label>
                        <textarea
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            className="w-full border px-3 py-2 rounded"
                            required
                        />
                    </div>

                    <div>
                        <label className="block mb-1">Priority</label>
                        <select
                            name="priority"
                            value={form.priority}
                            onChange={handleChange}
                            className="w-full border px-3 py-2 rounded"
                        >
                            <option value="LOW">LOW</option>
                            <option value="MEDIUM">MEDIUM</option>
                            <option value="HIGH">HIGH</option>
                            <option value="CRITICAL">CRITICAL</option>
                        </select>
                    </div>

                    <div>
                        <label className="block mb-1">Story Points (1–20)</label>
                        <input
                            type="number"
                            name="storyPoints"
                            value={form.storyPoints}
                            onChange={handleChange}
                            className="w-full border px-3 py-2 rounded"
                            required
                            min="1"
                            max="20"
                        />
                    </div>

                    <div>
                        <label className="block mb-1">Estimated Hours (1–200)</label>
                        <input
                            type="number"
                            name="estimatedHours"
                            value={form.estimatedHours}
                            onChange={handleChange}
                            className="w-full border px-3 py-2 rounded"
                            required
                            min="1"
                            max="200"
                        />
                    </div>

                    <div>
                        <label className="block mb-1">Due Date</label>
                        <input
                            type="date"
                            name="dueDate"
                            value={form.dueDate}
                            onChange={handleChange}
                            className="w-full border px-3 py-2 rounded"
                            required
                        />
                    </div>

                    {/* BUTTONS */}
                    <div className="flex flex-col gap-3 mt-6">

                        <button
                            type="button"
                            onClick={() => createTask(false)}
                            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                            disabled={loading}
                        >
                            {loading ? "Creating..." : "Create Task"}
                        </button>

                        <button
                            type="button"
                            onClick={() => createTask(true)}
                            className="w-full bg-purple-600 text-white py-2 rounded hover:bg-purple-700"
                            disabled={loading}
                        >
                            {loading ? "Creating..." : "Create & Get AI Recommendations"}
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate(`/projects/${projectId}`)}
                            className="w-full bg-gray-300 py-2 rounded hover:bg-gray-400"
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </DashboardLayout>
    );
}
