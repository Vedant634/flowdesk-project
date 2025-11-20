import { useState } from "react";
import api from "../../assets/axios";
import DashboardLayout from "../../layouts/DashboardLayout";
import { useNavigate } from "react-router-dom";

export default function CreateTeam() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        description: ""
    });

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const res = await api.post("/teams", form);
            alert("Team created successfully!");
            navigate("/teams");
        } catch (err) {
            console.error("Team creation failed", err);
            alert(err.response?.data?.message || "Failed to create team");
        }
    };

    return (
        <DashboardLayout>
            <h1 className="text-3xl font-bold mb-6 text-center">Create Team</h1>

            <form
                onSubmit={handleSubmit}
                className="bg-white p-6 rounded-xl shadow max-w-xl mx-auto"
            >
                {/* Team Name */}
                <label className="font-semibold">Team Name</label>
                <input
                    type="text"
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    className="w-full p-2 border rounded mb-4"
                    placeholder="Engineering Team"
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
                    placeholder="Core development team for FlowDesk"
                    required
                />

                <button
                    type="submit"
                    className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg w-full mt-4"
                >
                    Create Team
                </button>
            </form>
        </DashboardLayout>
    );
}
