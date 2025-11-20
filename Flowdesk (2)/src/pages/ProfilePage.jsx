import { useState, useEffect } from "react";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import { getCurrentUser, saveUser } from "../utils/auth";

export default function ProfilePage() {
    const user = getCurrentUser();
    const [form, setForm] = useState({
        firstName: user?.firstName || "",
        lastName: user?.lastName || "",
        skills: user?.skills || [],
    });
    const [skillInput, setSkillInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const addSkill = () => {
        if (!skillInput.trim()) return;
        setForm({ ...form, skills: [...form.skills, skillInput.trim()] });
        setSkillInput("");
    };

    const removeSkill = (skill) => {
        setForm({ ...form, skills: form.skills.filter((s) => s !== skill) });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const res = await api.put(`/users/${user.id}`, {
                firstName: form.firstName,
                lastName: form.lastName,
            });
            saveUser(res.data);
            setMessage("Profile updated successfully!");
            setTimeout(() => setMessage(""), 3000);
        } catch (err) {
            setMessage("Failed to update profile");
        } finally {
            setLoading(false);
        }
    };

    return (
        <DashboardLayout>
            <div className="max-w-2xl mx-auto">
                <h1 className="text-3xl font-bold mb-8">My Profile</h1>

                <div className="bg-white rounded-lg shadow p-8">
                    {message && (
                        <div className={`p-4 rounded-lg mb-6 ${message.includes("successfully") ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"}`}>
                            {message}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label className="block text-sm font-semibold mb-2">Email</label>
                            <input
                                type="email"
                                value={user?.email}
                                disabled
                                className="w-full p-3 border rounded-lg bg-gray-50 text-gray-600"
                            />
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-semibold mb-2">First Name</label>
                                <input
                                    type="text"
                                    name="firstName"
                                    value={form.firstName}
                                    onChange={handleChange}
                                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-semibold mb-2">Last Name</label>
                                <input
                                    type="text"
                                    name="lastName"
                                    value={form.lastName}
                                    onChange={handleChange}
                                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-semibold mb-2">Role</label>
                            <input
                                type="text"
                                value={user?.role}
                                disabled
                                className="w-full p-3 border rounded-lg bg-gray-50 text-gray-600"
                            />
                        </div>

                        {user?.role === "DEVELOPER" && (
                            <div>
                                <label className="block text-sm font-semibold mb-2">Skills</label>
                                <div className="flex gap-2 mb-3">
                                    <input
                                        type="text"
                                        value={skillInput}
                                        onChange={(e) => setSkillInput(e.target.value)}
                                        placeholder="Add a skill"
                                        className="flex-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
                                    />
                                    <button
                                        type="button"
                                        onClick={addSkill}
                                        className="bg-blue-600 text-white px-4 rounded-lg hover:bg-blue-700"
                                    >
                                        Add
                                    </button>
                                </div>
                                <div className="flex flex-wrap gap-2">
                                    {form.skills.map((skill, i) => (
                                        <span
                                            key={i}
                                            className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm flex items-center gap-2"
                                        >
                                            {skill}
                                            <button
                                                type="button"
                                                onClick={() => removeSkill(skill)}
                                                className="text-red-600 font-bold"
                                            >
                                                ✕
                                            </button>
                                        </span>
                                    ))}
                                </div>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50"
                        >
                            {loading ? "Saving..." : "Save Changes"}
                        </button>
                    </form>
                </div>
            </div>
        </DashboardLayout>
    );
}
