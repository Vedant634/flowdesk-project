import { useState, useEffect } from "react";
import DashboardLayout from "../../layouts/DashboardLayout";
import api from "../../assets/axios";
import { useNavigate } from "react-router-dom";

export default function CreateProject() {
    const navigate = useNavigate();

    const [teams, setTeams] = useState([]);
    const [form, setForm] = useState({
        name: "",
        description: "",
        teamId: "",
        startDate: "",
        endDate: "",
        githubRepoUrl: ""
    });

    useEffect(() => {
        async function loadTeams() {
            try {
                const res = await api.get("/teams");
                setTeams(res.data);
            } catch (e) {
                console.log("Failed to load teams", e);
            }
        }
        loadTeams();
    }, []);

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await api.post("/projects", form);
            alert("Project created successfully!");
            navigate(`/projects/${res.data.id}`);
        } catch (err) {
            console.log("Project creation failed", err);
            alert(err.response?.data?.message || "Error creating project");
        }
    };

    return (
        <DashboardLayout>

            <div className="flex justify-center">
                <div className="w-full max-w-2xl">
                    <h1 className="text-3xl font-bold mb-6 text-center">Create New Project</h1>

                    <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow">

                        {/* Project Name */}
                        <label className="font-semibold">Project Name</label>
                        <input
                            type="text"
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            className="w-full p-2 border rounded mb-4"
                            required
                        />

                        {/* Description */}
                        <label className="font-semibold">Description</label>
                        <textarea
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            className="w-full p-2 border rounded mb-4"
                            rows="3"
                            required
                        />

                        {/* Team */}
                        <label className="font-semibold">Assign Team</label>
                        <select
                            name="teamId"
                            value={form.teamId}
                            onChange={handleChange}
                            className="w-full p-2 border rounded mb-4"
                            required
                        >
                            <option value="">Select a Team</option>
                            {teams.map((t) => (
                                <option key={t.id} value={t.id}>{t.name}</option>
                            ))}
                        </select>

                        {/* Start & End Date */}
                        <label className="font-semibold">Start Date</label>
                        <input
                            type="date"
                            name="startDate"
                            value={form.startDate}
                            onChange={handleChange}
                            className="w-full p-2 border rounded mb-4"
                            required
                        />

                        <label className="font-semibold">End Date</label>
                        <input
                            type="date"
                            name="endDate"
                            value={form.endDate}
                            onChange={handleChange}
                            className="w-full p-2 border rounded mb-4"
                            required
                        />

                        {/* GitHub Repo URL */}
                        <label className="font-semibold">GitHub Repository URL</label>
                        <input
                            type="text"
                            name="githubRepoUrl"
                            value={form.githubRepoUrl}
                            onChange={handleChange}
                            className="w-full p-2 border rounded mb-4"
                            placeholder="https://github.com/user/repo"
                        />

                        <button
                            type="submit"
                            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg w-full mt-4"
                        >
                            Create Project
                        </button>
                    </form>
                </div>
            </div>

        </DashboardLayout>

    );
}
