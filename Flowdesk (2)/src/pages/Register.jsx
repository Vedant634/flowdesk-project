import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../assets/axios";
import { saveUser } from "../utils/auth";

export default function Register() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
        role: "",
        skills: [],
    });

    const [skillInput, setSkillInput] = useState("");
    const [errors, setErrors] = useState("");
    const [loading, setLoading] = useState(false);

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
        setErrors("");
        setLoading(true);

        if (
            !form.firstName ||
            !form.lastName ||
            !form.email ||
            !form.password ||
            !form.confirmPassword ||
            !form.role
        ) {
            setErrors("All fields are required.");
            setLoading(false);
            return;
        }

        if (!/\S+@\S+\.\S+/.test(form.email)) {
            setErrors("Invalid email format.");
            setLoading(false);
            return;
        }

        if (form.password !== form.confirmPassword) {
            setErrors("Passwords do not match.");
            setLoading(false);
            return;
        }

        try {
            const payload = {
                firstName: form.firstName,
                lastName: form.lastName,
                email: form.email,
                password: form.password,
                role: form.role,
                skills: form.role === "DEVELOPER" ? form.skills : [],
            };

            const res = await api.post("/auth/register", payload);
            localStorage.setItem("token", res.data.accessToken);
            saveUser(res.data.user);
            navigate(res.data.user.role === "MANAGER" ? "/" : "/developer-dashboard");
        } catch (err) {
            setErrors(err.response?.data?.message || "Registration failed.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-200 via-blue-300 to-blue-400 px-2">
            <div className="backdrop-blur-xl bg-white/70 shadow-2xl w-full max-w-4xl mx-auto rounded-3xl p-12 border border-white/50">
                <h1 className="text-4xl font-extrabold text-blue-800 text-center">
                    Create Your Account
                </h1>
                <p className="text-gray-700 text-center mt-2">
                    Join FlowDesk and manage tasks effortlessly.
                </p>

                {errors && (
                    <p className="mt-6 text-red-600 font-semibold text-center bg-red-100 py-3 rounded-xl">
                        {errors}
                    </p>
                )}

                <form className="mt-10 space-y-8" onSubmit={handleSubmit}>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <input
                            type="text"
                            name="firstName"
                            placeholder="First Name"
                            value={form.firstName}
                            onChange={handleChange}
                            className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full placeholder-gray-500"
                            required
                        />
                        <input
                            type="text"
                            name="lastName"
                            placeholder="Last Name"
                            value={form.lastName}
                            onChange={handleChange}
                            className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full placeholder-gray-500"
                            required
                        />
                    </div>

                    <input
                        type="email"
                        name="email"
                        placeholder="Email Address"
                        value={form.email}
                        onChange={handleChange}
                        className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full placeholder-gray-500"
                        required
                    />

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <input
                            type="password"
                            name="password"
                            placeholder="Create Password"
                            value={form.password}
                            onChange={handleChange}
                            className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full placeholder-gray-500"
                            required
                        />

                        <input
                            type="password"
                            name="confirmPassword"
                            placeholder="Confirm Password"
                            value={form.confirmPassword}
                            onChange={handleChange}
                            className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full placeholder-gray-500"
                            required
                        />
                    </div>

                    <select
                        name="role"
                        value={form.role}
                        onChange={handleChange}
                        className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full text-gray-600"
                        required
                    >
                        <option value="">Select Your Role</option>
                        <option value="MANAGER">Manager</option>
                        <option value="DEVELOPER">Developer</option>
                    </select>

                    {form.role === "DEVELOPER" && (
                        <div className="mt-4">
                            <label className="font-semibold text-gray-800 text-lg">Skills</label>

                            <div className="flex gap-4 mt-3">
                                <input
                                    type="text"
                                    value={skillInput}
                                    onChange={(e) => setSkillInput(e.target.value)}
                                    placeholder="e.g., React, Java, MongoDB"
                                    className="p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 w-full placeholder-gray-500"
                                />

                                <button
                                    type="button"
                                    onClick={addSkill}
                                    className="bg-blue-700 text-white px-7 rounded-2xl font-semibold hover:bg-blue-800 transition"
                                >
                                    Add
                                </button>
                            </div>

                            <div className="flex flex-wrap gap-3 mt-5">
                                {form.skills.map((skill, index) => (
                                    <span
                                        key={index}
                                        className="bg-blue-100 text-blue-700 px-4 py-2 rounded-full whitespace-nowrap flex items-center gap-2 font-medium border border-blue-200"
                                    >
                                        {skill}
                                        <button
                                            type="button"
                                            className="text-red-600 font-bold"
                                            onClick={() => removeSkill(skill)}
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
                        className="w-full bg-blue-700 text-white py-4 rounded-2xl text-xl font-bold shadow-lg hover:bg-blue-800 transition disabled:opacity-50"
                    >
                        {loading ? "Registering..." : "Register"}
                    </button>
                </form>

                <p className="mt-8 text-center text-gray-800">
                    Already have an account?{" "}
                    <Link to="/login" className="text-blue-700 font-semibold hover:underline">
                        Login here
                    </Link>
                </p>
            </div>
        </div>
    );
}
