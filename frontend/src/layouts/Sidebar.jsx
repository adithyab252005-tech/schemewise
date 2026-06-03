import { useLocation, Link, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, Search, FileText, Bookmark, 
  Activity, Bell, Settings, FileCheck, Layers, Target, LogOut, MapPin
} from 'lucide-react';
import { useProfile } from '../context/ProfileContext';

const MAIN_NAV = [
    { name: 'Overview',      path: '/dashboard',     icon: LayoutDashboard },
    { name: 'Directory',     path: '/explore',       icon: Search },
    { name: 'Eligibility',   path: '/results',       icon: FileCheck },
    { name: 'Saved',         path: '/saved',         icon: Bookmark },
    { name: 'Civic Index',   path: '/civic',         icon: Target },
];

const TOOLS_NAV = [
    { name: 'Simulator',     path: '/simulator',     icon: Activity },
    { name: 'Centers',       path: '/centers',       icon: MapPin },
    { name: 'Updates',       path: '/updates',       icon: Bell },
    { name: 'Settings',      path: '/settings',      icon: Settings },
];

const NavItem = ({ item, pathname }) => {
    const isActive = pathname === item.path || pathname.startsWith(`${item.path}/`);
    return (
        <Link to={item.path}
            className={`relative flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 group ${
                isActive
                    ? 'bg-white dark:bg-[#11151C] text-slate-900 dark:text-white shadow-sm border border-slate-100 dark:border-white/5'
                    : 'text-slate-500 dark:text-zinc-400 hover:bg-slate-100/80 dark:hover:bg-[#11151C]/60 hover:text-slate-700 dark:hover:text-white'
            }`}>
            {isActive && <div className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 bg-[#FF5E00] rounded-r-full shadow-[0_0_8px_rgba(255,94,0,0.7)]"></div>}
            <item.icon 
                size={17} 
                className={`transition-all duration-200 ${isActive ? 'text-[#FF5E00]' : 'group-hover:text-slate-700 dark:group-hover:text-white'}`} 
                strokeWidth={isActive ? 2.5 : 2}
            />
            <span className="font-semibold text-sm tracking-tight">{item.name}</span>
            {isActive && (
                <div className="ml-auto w-1.5 h-1.5 rounded-full bg-[#FF5E00] shadow-[0_0_6px_rgba(255,94,0,0.8)]" />
            )}
        </Link>
    );
};

const Sidebar = () => {
    const { pathname } = useLocation();
    const navigate = useNavigate();
    const { getActiveProfile, logout } = useProfile();
    const profile = getActiveProfile();
    const displayName = profile?.name?.split(' ')[0] || 'User';
    const avatarLetter = displayName.charAt(0).toUpperCase();

    const handleLogout = () => { logout(); navigate('/'); };

    return (
        <aside className="hidden md:flex w-[240px] shrink-0 h-full bg-slate-50 dark:bg-[#0B0E14] flex-col z-40 overflow-hidden border-r border-slate-100 dark:border-white/5 relative">
            {/* Side glow accent */}
            <div className="absolute top-0 right-0 w-px h-full bg-gradient-to-b from-transparent via-[#FF5E00]/20 to-transparent pointer-events-none" />

            {/* Logo */}
            <div className="h-[64px] px-5 flex items-center gap-3 border-b border-slate-100 dark:border-white/5 shrink-0">
                <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-[#FF7E20] to-[#FF3D00] shadow-[0_0_16px_rgba(255,94,0,0.4)] flex items-center justify-center shrink-0">
                    <Layers size={16} className="text-white drop-shadow-md" strokeWidth={2.5} />
                </div>
                <span className="text-lg font-black tracking-tight text-slate-900 dark:text-white leading-none">SchemeWise<span className="text-[#FF5E00]">.</span></span>
            </div>

            {/* Navigation */}
            <div className="flex-1 overflow-y-auto px-3 py-5 space-y-5">
                <div className="space-y-0.5">
                    {MAIN_NAV.map((item) => (
                        <NavItem key={item.path} item={item} pathname={pathname} />
                    ))}
                </div>

                <div>
                    <h3 className="px-3 text-[9px] font-black text-slate-400 dark:text-zinc-500 uppercase tracking-[0.25em] mb-2">Workspace</h3>
                    <div className="space-y-0.5">
                        {/* AI Agent — special accent */}
                        <Link to="/bot"
                            className={`relative flex items-center justify-between px-3 py-2.5 rounded-xl transition-all duration-200 group ${
                                pathname === '/bot'
                                    ? 'bg-white dark:bg-[#11151C] text-[#FF5E00] shadow-sm border border-slate-100 dark:border-[#FF5E00]/20'
                                    : 'text-[#FF5E00]/70 hover:bg-slate-100/80 dark:hover:bg-[#11151C]/60 hover:text-[#FF5E00]'
                            }`}>
                            {pathname === '/bot' && <div className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 bg-[#FF5E00] rounded-r-full shadow-[0_0_8px_rgba(255,94,0,0.7)]" />}
                            <div className="flex items-center gap-3">
                                <span className="text-sm font-black w-[17px] text-center drop-shadow-[0_0_5px_rgba(255,94,0,0.8)]">✦</span>
                                <span className="font-semibold text-sm tracking-tight">AI Agent</span>
                            </div>
                            {pathname === '/bot' && <div className="w-1.5 h-1.5 rounded-full bg-[#FF5E00] shadow-[0_0_6px_rgba(255,94,0,0.8)]" />}
                        </Link>
                        {TOOLS_NAV.map((item) => (
                            <NavItem key={item.path} item={item} pathname={pathname} />
                        ))}
                    </div>
                </div>
            </div>

            {/* Profile + Logout Footer */}
            <div className="px-3 pb-4 pt-3 border-t border-slate-100 dark:border-white/5 shrink-0 space-y-1">
                <Link to="/profile" className="flex items-center gap-3 px-3 py-2.5 rounded-xl hover:bg-slate-100/80 dark:hover:bg-[#11151C]/60 transition-all duration-200 group cursor-pointer">
                    <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-slate-200 to-slate-100 dark:from-zinc-700 dark:to-zinc-800 flex items-center justify-center font-black text-xs text-slate-600 dark:text-zinc-300 ring-1 ring-slate-200 dark:ring-white/10 shrink-0">
                        {avatarLetter}
                    </div>
                    <div className="flex flex-col min-w-0">
                        <span className="text-sm font-bold text-slate-800 dark:text-zinc-100 truncate">{displayName}</span>
                        <span className="text-[10px] text-slate-400 dark:text-zinc-500 font-medium">View Profile</span>
                    </div>
                </Link>

                <button
                    onClick={handleLogout}
                    className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-slate-500 dark:text-zinc-400 hover:bg-rose-50 dark:hover:bg-rose-500/10 hover:text-rose-600 dark:hover:text-rose-400 transition-all duration-200 group"
                >
                    <LogOut size={16} strokeWidth={2} className="transition-transform duration-200 group-hover:-translate-x-0.5" />
                    <span className="text-sm font-semibold">Sign Out</span>
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
