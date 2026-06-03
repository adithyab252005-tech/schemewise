import { useState, useEffect } from 'react';
import axios from 'axios';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await axios.get('http://180.235.121.245:8060/api/v1/admin/users');
            setUsers(res.data);
        } catch (error) {
            console.error("Failed to fetch users", error);
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleBan = async (id) => {
        try {
            const res = await axios.post(`http://180.235.121.245:8060/api/v1/admin/users/${id}/ban`, {}, {
                headers: { Authorization: `Bearer ${sessionStorage.getItem('schemewise_token')}` }
            });
            // Update local state to reflect new ban status
            setUsers(users.map(u => u.id === id ? { ...u, is_banned: res.data.is_banned } : u));
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.error || "Failed to toggle ban status");
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure you want to permanently delete this user? This cannot be undone.")) return;

        try {
            await axios.delete(`http://180.235.121.245:8060/api/v1/admin/users/${id}`, {
                headers: { Authorization: `Bearer ${sessionStorage.getItem('schemewise_token')}` }
            });
            // Remove user from local state
            setUsers(users.filter(u => u.id !== id));
        } catch (error) {
            console.error(error);
            alert(error.response?.data?.error || "Failed to delete user");
        }
    };

    return (
        <div className="max-w-6xl mx-auto">
            <div className="mb-8">
                <h1 className="text-3xl font-display font-bold text-slate-900 dark:text-white tracking-tight mb-2">User Management</h1>
                <p className="text-slate-500 dark:text-zinc-400">Govern platform access and roles.</p>
            </div>

            <div className="bg-slate-50 dark:bg-zinc-950 border border-slate-300 dark:border-zinc-900 rounded-xl overflow-hidden">
                <div className="overflow-x-auto custom-scrollbar-dark">
                    {loading ? (
                        <div className="p-8 text-center text-slate-500 dark:text-zinc-400">Loading Users...</div>
                    ) : (
                        <table className="w-full text-left text-sm text-slate-500 dark:text-zinc-400">
                            <thead className="text-xs text-slate-500 dark:text-zinc-400 uppercase bg-white dark:bg-white dark:bg-zinc-900/50">
                                <tr>
                                    <th className="px-6 py-4 font-medium">User ID</th>
                                    <th className="px-6 py-4 font-medium">Email</th>
                                    <th className="px-6 py-4 font-medium">State</th>
                                    <th className="px-6 py-4 font-medium">Role</th>
                                    <th className="px-6 py-4 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-zinc-900/60">
                                {users.map(u => (
                                    <tr key={u.id} className="hover:bg-white dark:bg-white/60 dark:bg-zinc-900/30 transition-colors">
                                        <td className="px-6 py-4 font-mono text-xs text-slate-500 dark:text-zinc-400">{u.id}</td>
                                        <td className="px-6 py-4 text-slate-500 dark:text-zinc-400 font-medium">{u.email}</td>
                                        <td className="px-6 py-4">{u.state}</td>
                                        <td className="px-6 py-4">
                                            {u.is_admin ? (
                                                <span className="px-2.5 py-1 rounded-full bg-brand-500/10 text-brand-400 text-xs font-semibold border border-brand-500/20">Admin</span>
                                            ) : u.is_banned ? (
                                                <span className="px-2.5 py-1 rounded-full bg-rose-500/10 text-rose-500 text-xs font-semibold border border-rose-500/20">Banned</span>
                                            ) : (
                                                <span className="px-2.5 py-1 rounded-full bg-white dark:bg-zinc-900 text-slate-500 dark:text-zinc-400 text-xs font-semibold border border-slate-200 dark:border-white/5">Citizen</span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 text-right space-x-3">
                                            {!u.is_admin && (
                                                <>
                                                    <button onClick={() => handleBan(u.id)} className={`${u.is_banned ? 'text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:text-white' : 'text-amber-500 hover:text-amber-400'} font-medium transition-colors`}>
                                                        {u.is_banned ? 'Unban' : 'Ban'}
                                                    </button>
                                                    <button onClick={() => handleDelete(u.id)} className="text-rose-500 hover:text-rose-400 font-medium transition-colors">Delete</button>
                                                </>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
};

export default UserManagement;
