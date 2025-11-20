import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../assets/axios";

export default function Login() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            console.log("Attempting login for:", email);
            const res = await api.post("/auth/login", { email, password });

            localStorage.setItem("token", res.data.accessToken);
            localStorage.setItem("currentUser", JSON.stringify(res.data.user));
            localStorage.setItem("userId", res.data.user.id);   // REQUIRED
            localStorage.setItem("role", res.data.user.role);   // optional but helpful


            console.log("Login successful, user role:", res.data.user.role);
            navigate("/");

        } catch (err) {
            console.error("[v0] Login failed:", err);
            if (err.code === "ERR_NETWORK") {
                setError("Cannot connect to server. Please ensure the backend is running on http://localhost:8080");
            } else {
                setError(err.response?.data?.message || err.response?.data?.error || "Invalid credentials");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-200 via-blue-300 to-blue-400 px-2 ">

            {/* GLASS CARD */}
            <div className="backdrop-blur-xl bg-white/70 shadow-2xl w-full max-w-xl mx-auto rounded-3xl p-12 border border-white/50">

                <h2 className="text-4xl font-extrabold text-blue-800 text-center">
                    Welcome Back
                </h2>
                <p className="text-gray-700 text-center mt-2">
                    Login to continue to FlowDesk
                </p>

                {error && (
                    <p className="mt-6 text-red-600 font-semibold text-center bg-red-100 py-3 rounded-xl">
                        {error}
                    </p>
                )}

                <form className="mt-10 space-y-7" onSubmit={handleLogin}>
                    {/* Email */}
                    <input
                        type="email"
                        placeholder="Email Address"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 placeholder-gray-500"
                        required
                    />

                    {/* Password */}
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full p-4 border rounded-2xl shadow-sm focus:ring-2 focus:ring-blue-500 placeholder-gray-500"
                        required
                    />

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-blue-700 text-white py-4 rounded-2xl text-xl font-bold shadow-lg hover:bg-blue-800 transition disabled:opacity-50"
                    >
                        {loading ? "Signing In..." : "Sign In"}
                    </button>
                </form>

                <p className="mt-8 text-center text-gray-800">
                    Don't have an account?{" "}
                    <Link to="/register" className="text-blue-700 font-semibold hover:underline">
                        Register here
                    </Link>
                </p>

            </div>
        </div>
    );
}
