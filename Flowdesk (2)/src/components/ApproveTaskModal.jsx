// ApproveTaskModal.jsx

import { useState } from "react";
import api from "../assets/axios";

export default function ApproveTaskModal({ taskId, onClose, onApproved }) {
    const [comment, setComment] = useState("");

    const handleApprove = async () => {
        try {
            await api.post(`/tasks/${taskId}/approve`, { comment });
            onApproved();
            onClose();
        } catch (err) {
            console.error(err);
            alert("Approval failed");
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center">
            <div className="bg-white p-6 rounded-xl w-96 shadow-lg">
                <h2 className="text-xl font-semibold mb-4">Approve Task</h2>

                <textarea
                    placeholder="Approval Comment"
                    className="w-full border rounded p-2 mb-3"
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                />

                <div className="flex justify-end gap-3">
                    <button onClick={onClose} className="px-4 py-2 bg-gray-200 rounded">
                        Cancel
                    </button>
                    <button
                        onClick={handleApprove}
                        className="px-4 py-2 bg-green-600 text-white rounded"
                    >
                        Approve
                    </button>
                </div>
            </div>
        </div>
    );
}
