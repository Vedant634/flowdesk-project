import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import { ArrowLeft } from 'lucide-react';

export default function ProjectDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [project, setProject] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadProject();
    }, [id]);

    const loadProject = async () => {
        try {
            const [projRes, tasksRes] = await Promise.all([
                api.get(`/projects/${id}`),
                api.get(`/projects/${id}/tasks`),
            ]);
            setProject(projRes.data);
            setTasks(tasksRes.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <DashboardLayout><p className="text-center mt-20">Loading...</p></DashboardLayout>;
    if (!project) return <DashboardLayout><p className="text-center mt-20">Project not found</p></DashboardLayout>;

    const tasksByStatus = {
        TODO: tasks.filter(t => t.status === "TODO"),
        IN_PROGRESS: tasks.filter(t => t.status === "IN_PROGRESS"),
        IN_REVIEW: tasks.filter(t => t.status === "IN_REVIEW"),
        DONE: tasks.filter(t => t.status === "DONE"),
    };

    return (
        <DashboardLayout>
            {/* Back Button */}
            <button
                onClick={() => navigate("/projects")}
                className="flex items-center gap-2 text-blue-600 hover:text-blue-800 mb-6"
            >
                <ArrowLeft size={20} />
                Back to Projects
            </button>

            {/* Create Task Button */}
            <button
                onClick={() => navigate(`/projects/${id}/tasks/create`)}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg shadow hover:bg-blue-700 mb-6"
            >
                + Create Task
            </button>


            <div className="bg-white rounded-lg shadow p-6 mb-8">
                <div className="flex items-start justify-between mb-4">
                    <div>
                        <h1 className="text-3xl font-bold">{project.name}</h1>
                        <p className="text-gray-600 mt-2">{project.description}</p>
                    </div>
                    <span className="px-4 py-2 bg-blue-100 text-blue-800 rounded-full font-semibold">
                        {project.status}
                    </span>
                </div>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
                    <div>
                        <p className="text-gray-600 text-sm">Total Tasks</p>
                        <p className="text-2xl font-bold">{tasks.length}</p>
                    </div>
                    <div>
                        <p className="text-gray-600 text-sm">Completed</p>
                        <p className="text-2xl font-bold">{tasksByStatus.DONE.length}</p>
                    </div>
                    <div>
                        <p className="text-gray-600 text-sm">Story Points</p>
                        <p className="text-2xl font-bold">{project.completedStoryPoints}/{project.totalStoryPoints}</p>
                    </div>
                    <div>
                        <p className="text-gray-600 text-sm">Risk Level</p>
                        <p className="text-2xl font-bold text-red-600">{project.riskLevel}</p>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                {["TODO", "IN_PROGRESS", "IN_REVIEW", "DONE"].map((status) => (
                    <div key={status} className="bg-gray-50 rounded-lg p-4">
                        <h3 className="font-bold text-lg mb-4">{status.replace(/_/g, " ")}</h3>
                        <div className="space-y-2">
                            {tasksByStatus[status].map((task) => (
                                <div
                                    key={task.id}
                                    className="bg-white p-3 rounded-lg shadow-sm hover:shadow-md cursor-pointer"
                                    onClick={() => navigate(`/tasks/${task.id}`)}
                                >
                                    <p className="font-semibold text-sm">{task.title}</p>
                                    <div className="flex items-center justify-between mt-2 text-xs text-gray-600">
                                        <span>Points: {task.storyPoints}</span>
                                        <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded">
                                            {task.priority}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </DashboardLayout>
    );
}
