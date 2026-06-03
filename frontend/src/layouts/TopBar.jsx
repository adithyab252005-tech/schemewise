import { useState, useEffect } from 'react';
import { useProfile } from '../context/ProfileContext';
import { LogOut, Menu, X, Bell, Settings, Sun, Moon, Search } from 'lucide-react';
import { useNavigate, Link, useLocation } from 'react-router-dom';

const TopBar = () => {
    const { getActiveProfile, logout } = useProfile();
    const navigate = useNavigate();
    const { pathname } = useLocation();
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    
    // Theme Toggle Logic
    const [isDark, setIsDark] = useState(() => {
        if (typeof window !== 'undefined') {
            return document.documentElement.classList.contains('dark') || 
                   (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches);
        }
        return true;
    });

    useEffect(() => {
        if (isDark) {
            document.documentElement.classList.add('dark');
            localStorage.setItem('theme', 'dark');
        } else {
            document.documentElement.classList.remove('dark');
            localStorage.setItem('theme', 'light');
        }
    }, [isDark]);

    const profile = getActiveProfile();
    const displayName = profile?.name?.split(' ')[0] || 'User';
    const avatarLetter = displayName.charAt(0).toUpperCase();

    const handleLogout = () => { logout(); navigate('/'); };

    // Dynamic Breadcrumb based on path
    const getPageTitle = () => {
        if (pathname.includes('/dashboard')) return 'Overview';
        if (pathname.includes('/explore')) return 'Directory';
        if (pathname.includes('/saved')) return 'Saved Schemes';
        if (pathname.includes('/simulator')) return 'Rules Engine';
        if (pathname.includes('/bot')) return 'AI Assistant';
        if (pathname.includes('/results')) return 'Eligibility';
        return 'Dashboard';
    };

    return (
        <>
        <header className="h-[60px] md:h-[70px] bg-white/70 dark:bg-[#050505]/70 backdrop-blur-xl border-b border-slate-200/80 dark:border-white/5 flex items-center justify-between px-4 md:px-8 flex-shrink-0 z-50 sticky top-0 pt-[env(safe-area-inset-top)]" style={{height:'calc(70px + env(safe-area-inset-top))'}}>
            
            {/* Left: Dynamic Breadcrumb */}
            <div className="flex items-center gap-3 shrink-0">
                <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="md:hidden p-1.5 -ml-1.5 text-slate-500 hover:text-slate-900 dark:text-zinc-400 dark:hover:text-white transition-colors">
                    {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
                </button>
                <div className="flex items-center gap-2 text-sm font-medium">
                    <span className="text-slate-500 dark:text-zinc-400 hidden sm:block">SchemeWise</span>
                    <span className="text-slate-300 dark:text-zinc-600 hidden sm:block">/</span>
                    <span className="text-slate-900 dark:text-zinc-50 font-bold">{getPageTitle()}</span>
                </div>
            </div>

            <div className="flex-1 mx-8 hidden md:block">
            </div>

            {/* Right: Profile & Actions */}
            <div className="flex items-center gap-2 sm:gap-4 shrink-0">
                
                {/* Search Icon (Mobile mostly, or global search) */}
                <button className="p-2 text-slate-400 hover:text-slate-900 dark:text-zinc-500 dark:hover:text-zinc-200 transition-colors hidden sm:block">
                    <Search size={18} />
                </button>

                {/* Theme Toggle Button */}
                <button onClick={() => setIsDark(!isDark)} title="Toggle Theme"
                    className="p-2 text-slate-400 hover:text-slate-900 dark:text-zinc-500 dark:hover:text-zinc-200 transition-colors">
                    {isDark ? <Sun size={18} /> : <Moon size={18} />}
                </button>

                <div className="w-px h-4 bg-slate-200 dark:bg-zinc-800 hidden sm:block mx-1" />

                <Link to="/profile" className="flex items-center gap-2.5 group cursor-pointer pl-1 rounded-full hover:bg-slate-50 dark:hover:bg-zinc-900/50 pr-2 py-1 transition-colors">
                    <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-gradient-to-tr from-slate-200 to-slate-100 dark:from-zinc-800 dark:to-zinc-700 text-slate-600 dark:text-zinc-300 flex items-center justify-center font-bold text-xs shadow-sm ring-1 ring-slate-200 dark:ring-white/5">
                        {avatarLetter}
                    </div>
                    <span className="hidden sm:block text-sm font-medium text-slate-700 dark:text-zinc-300 group-hover:text-slate-900 dark:group-hover:text-white transition-colors">
                        {displayName}
                    </span>
                </Link>
            </div>
        </header>

        {/* Mobile Dropdown Menu (only renders when open) */}
        {mobileMenuOpen && (
            <div className="absolute top-[calc(60px+env(safe-area-inset-top))] left-0 right-0 bg-white/95 dark:bg-[#09090b]/95 backdrop-blur-xl border-b border-slate-200 dark:border-white/5 shadow-xl md:hidden flex flex-col z-40 animate-fade-in border-t border-slate-100 dark:border-white/5">
                <div className="p-3 flex flex-col gap-1 max-h-[70vh] overflow-y-auto">
                    {[
                        { to: '/results',      label: 'Eligibility' },
                        { to: '/civic',        label: 'Civic Index' },
                        { to: '/simulator',    label: 'Rules Engine Simulator' },
                        { to: '/centers',      label: 'Map Centers' },
                        { to: '/updates',      label: 'System Updates' },
                        { to: '/settings',     label: 'Settings' },
                    ].map(({ to, label }) => (
                        <Link key={to} to={to} onClick={() => setMobileMenuOpen(false)}
                            className="px-4 py-3 rounded-xl text-slate-600 dark:text-zinc-300 hover:bg-slate-50 dark:hover:bg-zinc-900 text-sm font-medium transition-colors">
                            {label}
                        </Link>
                    ))}
                    <div className="h-px bg-slate-100 dark:bg-zinc-800 my-2 mx-4" />
                    <button onClick={() => { setMobileMenuOpen(false); handleLogout(); }}
                        className="flex items-center gap-3 px-4 py-3 rounded-xl text-slate-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-500/10 text-sm font-medium w-full text-left transition-colors">
                        <LogOut size={16} /> Sign out
                    </button>
                </div>
            </div>
        )}
        </>
    );
};

export default TopBar;
