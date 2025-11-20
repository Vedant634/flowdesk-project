import DashboardLayout from "../layouts/DashboardLayout";
import StatsCard from "../components/StatsCard";
import WorkloadBar from "../components/WorkloadBar";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

export default function ManagerDashboard() {
    const navigate = useNavigate();

    // ---------- DUMMY DATA FOR SCREENSHOTS ----------
    const dummyData = {
        activeProjects: 2,
        totalTasks: 14,
        highRiskTasksCount: 2,
        completedTasks: 5,

        highRiskTasks: [
            {
                id: "t1",
                title: "Implement Payment Gateway",
                description: "High priority issue with API timeout failures."
            },
            {
                id: "t2",
                title: "Fix Kafka Consumer Lag",
                description: "Lag exceeds 5 seconds in peak traffic."
            }
        ],

        teamWorkload: [
            { id: 1, name: "Alice Johnson", load: 28, capacity: 40 },
            { id: 2, name: "Ravi Sharma", load: 35, capacity: 40 },
            { id: 3, name: "Neha Patel", load: 18, capacity: 40 }
        ],

        upcomingDeadlines: [
            {
                taskId: "d1",
                task: "JWT Authentication Module",
                project: "FlowDesk Backend",
                assignee: "Vedant",
                deadline: "25/11/2025"
            },
            {
                taskId: "d2",
                task: "UI Fixes for Dashboard",
                project: "FlowDesk Frontend",
                assignee: "Harsh",
                deadline: "28/11/2025"
            },
            {
                taskId: "d3",
                task: "Write Unit Tests",
                project: "FlowDesk API",
                assignee: "Deepak",
                deadline: "30/11/2025"
            }
        ]
    };

    const [data] = useState(dummyData);

    return (
        <DashboardLayout>
            <h1 className="text-3xl font-bold mb-8">Manager Dashboard</h1>

            {/* ACTION BUTTONS */}
            <div className="flex flex-wrap justify-between items-center mb-8 gap-4">
                <div className="flex gap-4">
                    <button
                        className="bg-gray-700 text-white px-4 py-2 rounded-lg shadow hover:bg-gray-800"
                        onClick={() => navigate("/projects")}
                    >
                        📁 View All Projects
                    </button>

                    <button
                        className="bg-gray-700 text-white px-4 py-2 rounded-lg shadow hover:bg-gray-800"
                        onClick={() => navigate("/teams")}
                    >
                        👥 View All Teams
                    </button>
                </div>

                <div className="flex gap-4">
                    <button
                        className="bg-blue-600 text-white px-4 py-2 rounded-lg shadow hover:bg-blue-700"
                        onClick={() => navigate("/projects/create")}
                    >
                        + New Project
                    </button>

                    <button
                        className="bg-green-600 text-white px-4 py-2 rounded-lg shadow hover:bg-green-700"
                        onClick={() => navigate("/teams/create")}
                    >
                        + New Team
                    </button>
                </div>
            </div>

            {/* STATS CARDS */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <StatsCard title="Active Projects" value={data.activeProjects} color="#3B82F6" />
                <StatsCard title="Total Tasks" value={data.totalTasks} color="#10B981" />
                <StatsCard title="High Risk Tasks" value={data.highRiskTasksCount} color="#EF4444" />
                <StatsCard title="Completed Tasks" value={data.completedTasks} color="#F59E0B" />
            </div>

            {/* HIGH RISK TASKS */}
            <div className="bg-white p-6 rounded-xl shadow mb-8">
                <h2 className="text-xl font-bold mb-4">High Risk Tasks</h2>

                {data.highRiskTasksCount === 0 ? (
                    <p className="text-gray-600">No high-risk tasks</p>
                ) : (
                    <div className="grid md:grid-cols-2 gap-4">
                        {data.highRiskTasks.map((task) => (
                            <div key={task.id} className="p-4 border rounded-lg">
                                <p className="font-bold">{task.title}</p>
                                <p className="text-sm text-gray-600">{task.description}</p>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* TEAM WORKLOAD */}
            <div className="bg-white p-6 rounded-xl shadow mb-8">
                <h2 className="text-xl font-bold mb-4">Team Workload</h2>

                {data.teamWorkload.length === 0 ? (
                    <p className="text-gray-600">No team workload data</p>
                ) : (
                    data.teamWorkload.map((w) => (
                        <WorkloadBar
                            key={w.id}
                            name={w.name}
                            load={w.load}
                            capacity={w.capacity}
                        />
                    ))
                )}
            </div>

            {/* UPCOMING DEADLINES */}
            <div className="bg-white p-6 rounded-xl shadow mb-8">
                <h2 className="text-xl font-bold mb-4">Upcoming Deadlines</h2>

                {data.upcomingDeadlines.length === 0 ? (
                    <p className="text-gray-600">No upcoming deadlines</p>
                ) : (
                    <table className="w-full text-sm">
                        <thead>
                            <tr className="text-left border-b">
                                <th className="py-2">Task</th>
                                <th className="py-2">Project</th>
                                <th className="py-2">Developer</th>
                                <th className="py-2">Deadline</th>
                            </tr>
                        </thead>

                        <tbody>
                            {data.upcomingDeadlines.map((d) => (
                                <tr key={d.taskId} className="border-b">
                                    <td className="py-2 font-medium">{d.task}</td>
                                    <td>{d.project}</td>
                                    <td>{d.assignee}</td>
                                    <td className="font-semibold">{d.deadline}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

        </DashboardLayout>
    );
}
