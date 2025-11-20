import { useEffect, useState } from "react";
import api from "../assets/axios";

export default function AddTeamMemberModal({ teamId, onClose, onAdded }) {
    const [developers, setDevelopers] = useState([]);
    const [selectedUserId, setSelectedUserId] = useState("");

    useEffect(() => {
        loadDevelopers();
    }, []);

    const loadDevelopers = async () => {
        try {
            const allUsers = await api.get("/users");

            // Filter ONLY developers
            const devs = allUsers.data.filter(u => u.role === "DEVELOPER");

            setDevelopers(devs);
        } catch (err) {
            console.error("Failed to load developers", err);
        }
    };

    const handleAdd = async () => {
        try {
            await api.post(`/teams/${teamId}/members`, { userId: selectedUserId });
            onAdded();
            onClose();
        } catch (err) {
            console.error("Failed to add team member", err);
            alert("Error adding member");
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center">
            <div className="bg-white p-6 rounded-lg w-96">
                <h2 className="text-xl font-bold mb-4">Add Developer</h2>

                <select
                    className="w-full border p-2 rounded mb-4"
                    value={selectedUserId}
                    onChange={e => setSelectedUserId(e.target.value)}
                >
                    <option value="">Select Developer</option>
                    {developers.map(dev => (
                        <option key={dev.id} value={dev.id}>
                            {dev.firstName} {dev.lastName}
                        </option>
                    ))}
                </select>

                <div className="flex justify-end gap-2">
                    <button onClick={onClose} className="px-3 py-2 bg-gray-200 rounded">Cancel</button>
                    <button onClick={handleAdd} className="px-3 py-2 bg-blue-600 text-white rounded">Add</button>
                </div>
            </div>
        </div>
    );
}
