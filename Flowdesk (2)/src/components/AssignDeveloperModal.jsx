import { useEffect, useState } from "react";
import api from "../assets/axios";

export default function AssignDeveloperModal({ taskId, onClose, onAssigned }) {
    const [developers, setDevelopers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDevelopers();
    }, []);

    const loadDevelopers = async () => {
        try {
            const res = await api.get("/users");

            // Filter only developers on frontend
            const devsOnly = res.data.filter(u => u.role === "DEVELOPER");

            setDevelopers(devsOnly);

        } catch (err) {
            console.error("Failed loading developers", err);
        } finally {
            setLoading(false);
        }
    };

    const handleAssign = async (userId) => {
        try {
            await api.post(`/tasks/${taskId}/assign`, { userId });
            onAssigned();
            onClose();
        } catch (err) {
            console.error("Assign failed", err);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center">
            <div className="bg-white p-6 rounded-lg w-96">

                <h2 className="text-xl font-bold mb-4">Assign Developer</h2>

                {loading ? (
                    <p>Loading developers...</p>
                ) : developers.length === 0 ? (
                    <p className="text-red-600">No developers found.</p>
                ) : (
                    <div className="space-y-3">
                        {developers.map((dev) => (
                            <button
                                key={dev.id}
                                onClick={() => handleAssign(dev.id)}
                                className="w-full p-2 border rounded hover:bg-gray-100 text-left"
                            >
                                {dev.firstName} {dev.lastName}
                            </button>
                        ))}
                    </div>
                )}

                <button
                    className="mt-4 w-full bg-gray-200 py-2 rounded"
                    onClick={onClose}
                >
                    Cancel
                </button>

            </div>
        </div>
    );
}
