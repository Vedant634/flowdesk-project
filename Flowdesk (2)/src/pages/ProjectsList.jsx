import { useEffect, useState } from "react";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import { Plus, ExternalLink } from 'lucide-react';
import { useNavigate } from "react-router-dom";

export default function ProjectsList() {
    const navigate = useNavigate();
    const [projects, setProjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        loadProjects();
    }, []);

    const loadProjects = async () => {
        try {
            const res = await api.get("/projects");
            setProjects(res.data);
        } catch (err) {
            setError("Failed to load projects");
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status) => {
        const colors = {
            PLANNING: "bg-blue-100 text-blue-800",
            ACTIVE: "bg-green-100 text-green-800",
            ON_HOLD: "bg-yellow-100 text-yellow-800",
            COMPLETED: "bg-gray-100 text-gray-800",
            CANCELLED: "bg-red-100 text-red-800",
        };
        return colors[status] || "bg-gray-100 text-gray-800";
    };

    const getRiskColor = (risk) => {
        const colors = {
            LOW: "text-green-600",
            MEDIUM: "text-yellow-600",
            HIGH: "text-red-600",
        };
        return colors[risk] || "text-gray-600";
    };

    if (loading) return <DashboardLayout><p className="text-center mt-20">Loading projects...</p></DashboardLayout>;

    return (
        <DashboardLayout>
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-bold">Projects</h1>
                <button className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
                    <Plus size={20} />
                    New Project
                </button>
            </div>

            {error && <p className="text-red-600 mb-4">{error}</p>}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {projects.map((project) => (
                    <div
                        key={project.id}
                        className="bg-white rounded-lg shadow hover:shadow-lg transition cursor-pointer p-6"
                        onClick={() => navigate(`/projects/${project.id}`)}
                    >
                        <div className="flex items-start justify-between mb-4">
                            <h3 className="text-lg font-bold text-gray-800">{project.name}</h3>
                            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(project.status)}`}>
                                {project.status}
                            </span>
                        </div>

                        <p className="text-gray-600 text-sm mb-4">{project.description}</p>

                        <div className="mb-4">
                            <div className="flex justify-between text-sm mb-1">
                                <span className="text-gray-600">Progress</span>
                                <span className="font-semibold">{project.completionPercentage || 0}%</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-2">
                                <div
                                    className="h-2 bg-blue-600 rounded-full"
                                    style={{ width: `${project.completionPercentage || 0}%` }}
                                />
                            </div>
                        </div>

                        <div className="flex items-center justify-between text-sm text-gray-600 mb-3">
                            <span>Points: {project.completedStoryPoints}/{project.totalStoryPoints}</span>
                            <span className={`font-semibold ${getRiskColor(project.riskLevel)}`}>
                                {project.riskLevel} Risk
                            </span>
                        </div>

                        {project.githubRepoUrl && (
                            <a
                                href={project.githubRepoUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="flex items-center gap-2 text-blue-600 hover:text-blue-800 text-sm"
                                onClick={(e) => e.stopPropagation()}
                            >
                                <ExternalLink size={14} />
                                GitHub Repo
                            </a>
                        )}
                    </div>
                ))}
            </div>
        </DashboardLayout>
    );
}
