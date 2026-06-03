import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useProfile } from '../../context/ProfileContext';
import { BarChart2, FolderOpen, Cpu, Users, LogOut, ShieldCheck } from 'lucide-react';

const AdminLayout = () => {
    const { getActiveProfile, logout } = useProfile();
    const navigate = useNavigate();
    const profile = getActiveProfile();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    if (!profile || !profile.is_admin) {
        return (
            <div className="min-h-screen flex items-center justify-center text-center p-8 bg-transparent">
                <div className="pro-card shadow-2xl shadow-black/5 dark:shadow-black/40 p-10 rounded-sm max-w-md">
                    <h1 className="text-3xl text-red-600 font-bold mb-4 uppercase">403 Forbidden</h1>
                    <p className="text-slate-500 dark:text-zinc-400 mb-8 text-sm">You do not have administrative privileges to access this area.</p>
                    <button onClick={() => navigate('/')} className="px-6 py-2 bg-brand-700 text-slate-900 dark:text-white rounded-sm text-sm font-medium border border-brand-800">Return Home</button>
                </div>
            </div>
        );
    }

    const navItems = [
        { path: '/admin/analytics', label: 'Platform Analytics', icon: BarChart2 },
        { path: '/admin/schemes', label: 'Scheme Management', icon: FolderOpen },
        { path: '/admin/ai-control', label: 'AI Control Center', icon: Cpu },
        { path: '/admin/users', label: 'User Management', icon: Users },
    ];

    return (
        <div className="flex h-screen overflow-hidden bg-transparent">
            {/* Admin Sidebar - NIC Style */}
            <div className="w-64 bg-slate-50 dark:bg-zinc-950 text-slate-800 dark:text-zinc-50 flex flex-col shadow-xl flex-shrink-0 z-20">
                {/* Logo */}
                <div className="h-16 flex items-center gap-3 px-5 border-b border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950">
                    <div className="w-8 h-8 rounded pro-card flex items-center justify-center shrink-0">
                        <ShieldCheck size={18} className="text-orange-500" />
                    </div>
                    <div>
                        <p className="font-bold text-slate-900 dark:text-white tracking-tight text-sm leading-tight">SchemeWise</p>
                        <p className="text-[10px] text-slate-500 dark:text-zinc-400 uppercase tracking-wider font-semibold">Admin Portal</p>
                    </div>
                </div>

                {/* Govt Ribbon */}
                <div className="px-5 py-2 bg-slate-50 dark:bg-zinc-950/90 border-b border-slate-300 dark:border-zinc-900">
                    <p className="text-[10px] text-slate-500 dark:text-zinc-400 uppercase tracking-widest font-medium">System Administration</p>
                </div>

                {/* Navigation */}
                <nav className="flex-1 py-4 px-3 flex flex-col gap-1 overflow-y-auto">
                    <div className="px-3 mb-2 text-[10px] font-semibold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">
                        Control Modules
                    </div>
                    {navItems.map((item) => {
                        const Icon = item.icon;
                        return (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                className={({ isActive }) =>
                                    `flex items-center gap-3 px-3 py-2.5 rounded text-sm font-medium transition-colors border-l-4 ${isActive
                                        ? 'bg-white dark:bg-zinc-900 text-slate-900 dark:text-white border-orange-500'
                                        : 'text-slate-500 dark:text-zinc-400 hover:bg-white dark:bg-white dark:bg-zinc-900/50 hover:text-slate-900 dark:text-white border-transparent'
                                    }`
                                }
                            >
                                {({ isActive }) => (
                                    <>
                                        <Icon size={17} className={isActive ? 'text-orange-500' : 'text-slate-500 dark:text-zinc-400'} />
                                        {item.label}
                                    </>
                                )}
                            </NavLink>
                        );
                    })}
                </nav>

                {/* Sign Out */}
                <div className="p-4 border-t border-slate-300 dark:border-zinc-900 bg-slate-50 dark:bg-zinc-950/50">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-3 py-2.5 w-full text-sm font-medium text-slate-500 dark:text-zinc-400 hover:text-red-400 hover:bg-red-900/20 rounded transition-colors border border-transparent hover:border-red-900/40"
                    >
                        <LogOut size={17} />
                        Sign Out
                    </button>
                </div>
            </div>

            {/* Main Content */}
            <div className="flex-1 overflow-y-auto bg-[#0a0f1c]">
                {/* Admin TopBar */}
                <div className="h-14 bg-[#0a0f1c] border-b border-slate-300 dark:border-zinc-900 flex items-center justify-between px-6 shadow-2xl shadow-black/5 dark:shadow-black/40">
                    <p className="text-base font-bold text-slate-600 dark:text-zinc-300 uppercase tracking-wide">Admin Control Panel</p>
                    <div className="flex items-center gap-3 text-sm text-slate-500 dark:text-zinc-400">
                        <span className="text-xs text-slate-500 dark:text-zinc-400 font-medium">{profile?.name || 'Admin'}</span>
                        <span className="px-2 py-0.5 bg-red-500/10 text-red-400 border border-red-500/20 text-xs font-bold uppercase rounded-sm">Restricted Access</span>
                    </div>
                </div>
                <div className="p-8">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default AdminLayout;
