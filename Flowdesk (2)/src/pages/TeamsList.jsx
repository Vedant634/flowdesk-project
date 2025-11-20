import { useEffect, useState } from "react";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import { Plus, Users } from 'lucide-react';
import { useNavigate } from "react-router-dom";

export default function TeamsList() {
    const navigate = useNavigate();
    const [teams, setTeams] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadTeams();
    }, []);

    const loadTeams = async () => {
        try {
            const res = await api.get("/teams");
            setTeams(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <DashboardLayout><p className="text-center mt-20">Loading teams...</p></DashboardLayout>;

    return (
        <DashboardLayout>
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-bold">Teams</h1>
                <button className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
                    <Plus size={20} />
                    New Team
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {teams.map((team) => (
                    <div
                        key={team.id}
                        className="bg-white rounded-lg shadow hover:shadow-lg transition cursor-pointer p-6"
                        onClick={() => navigate(`/teams/${team.id}`)}
                    >
                        <h3 className="text-xl font-bold mb-2">{team.name}</h3>
                        <p className="text-gray-600 text-sm mb-4">{team.description}</p>
                        <div className="flex items-center justify-between text-sm text-gray-600">
                            <div className="flex items-center gap-2">
                                <Users size={16} />
                                <span>{team.memberCount || 0} members</span>
                            </div>
                            <span className="text-blue-600 font-semibold">View Details</span>
                        </div>
                    </div>
                ))}
            </div>
        </DashboardLayout>
    );
}
