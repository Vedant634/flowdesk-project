import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import DashboardLayout from "../layouts/DashboardLayout";
import api from "../assets/axios";
import { ArrowLeft } from "lucide-react";
import WorkloadBar from "../components/WorkloadBar";
import AddTeamMemberModal from "../components/AddTeamMemberModal";

export default function TeamDetail() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [team, setTeam] = useState(null);
    const [members, setMembers] = useState([]);
    const [workload, setWorkload] = useState(null);
    const [loading, setLoading] = useState(true);

    const [showAddModal, setShowAddModal] = useState(false);

    const currentUser = JSON.parse(localStorage.getItem("currentUser"));
    const isManager = currentUser?.role === "MANAGER";

    useEffect(() => {
        loadTeamData();
    }, [id]);

    const loadTeamData = async () => {
        try {
            const teamRes = await api.get(`/teams/${id}`);
            const workloadRes = await api.get(`/teams/${id}/workload`);

            setTeam(teamRes.data);
            setMembers(teamRes.data.members || []);
            setWorkload(workloadRes.data);
        } catch (err) {
            console.error("Failed to load team data", err);
        } finally {
            setLoading(false);
        }
    };

    if (loading)
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Loading...</p>
            </DashboardLayout>
        );

    if (!team)
        return (
            <DashboardLayout>
                <p className="text-center mt-20">Team not found</p>
            </DashboardLayout>
        );

    return (
        <DashboardLayout>
            {/* BACK BUTTON */}
            <button
                onClick={() => navigate("/teams")}
                className="flex items-center gap-2 text-blue-600 hover:text-blue-800 mb-6"
            >
                <ArrowLeft size={20} />
                Back to Teams
            </button>

            {/* TEAM HEADER */}
            <div className="bg-white rounded-lg shadow p-6 mb-8">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold mb-2">{team.name}</h1>
                        <p className="text-gray-600">{team.description}</p>
                    </div>

                    {/* ➕ ADD MEMBER BUTTON (Manager only) */}
                    {isManager && (
                        <button
                            onClick={() => setShowAddModal(true)}
                            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                        >
                            Add Developer
                        </button>
                    )}
                </div>
            </div>

            {/* WORKLOAD SUMMARY */}
            {workload && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <div className="bg-white rounded-lg shadow p-6">
                        <p className="text-gray-600 text-sm">Total Members</p>
                        <p className="text-3xl font-bold mt-2">{workload.members?.length || 0}</p>
                    </div>

                    <div className="bg-white rounded-lg shadow p-6">
                        <p className="text-gray-600 text-sm">Average Utilization</p>
                        <p className="text-3xl font-bold mt-2">
                            {workload.averageUtilization?.toFixed(1) || 0}%
                        </p>
                    </div>

                    <div className="bg-white rounded-lg shadow p-6">
                        <p className="text-gray-600 text-sm">Team Status</p>
                        <p
                            className={`text-3xl font-bold mt-2 ${workload.isBalanced ? "text-green-600" : "text-red-600"
                                }`}
                        >
                            {workload.isBalanced ? "Balanced" : "Needs Rebalance"}
                        </p>
                    </div>
                </div>
            )}

            {/* WORKLOAD BARS */}
            <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-bold mb-4">Team Workload</h2>

                {workload?.members?.map((member) => (
                    <WorkloadBar
                        key={member.user?.id}
                        name={`${member.user?.firstName} ${member.user?.lastName}`}
                        load={member.currentWorkload || 0}
                        capacity={member.maxCapacity || 40}
                    />
                ))}

                {/* If empty */}
                {workload?.members?.length === 0 && (
                    <p className="text-gray-500 text-center mt-4">No members yet.</p>
                )}
            </div>

            {/* ADD MEMBER MODAL */}
            {showAddModal && (
                <AddTeamMemberModal
                    teamId={id}
                    onClose={() => setShowAddModal(false)}
                    onAdded={loadTeamData}
                />
            )}
        </DashboardLayout>
    );
}
