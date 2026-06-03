import { Link, useLocation } from 'react-router-dom';
import { Home, Search, Bot, Bookmark } from 'lucide-react';

const BottomNavBar = () => {
    const location = useLocation();

    const navItems = [
        { path: '/dashboard', label: 'Home', icon: Home },
        { path: '/explore', label: 'Schemes', icon: Search },
        { path: '/bot', label: 'AI Chat', icon: Bot },
        { path: '/saved', label: 'Saved', icon: Bookmark },
    ];

    return (
        <div className="w-full">
            <div className="flex justify-around items-center h-[72px] px-2 pb-[env(safe-area-inset-bottom)]">
                {navItems.map((item) => {
                    const isActive = location.pathname.startsWith(item.path);
                    const Icon = item.icon;
                    return (
                        <Link
                            key={item.path}
                            to={item.path}
                            className="flex flex-col items-center justify-center w-full h-full space-y-1.5 relative group"
                        >
                            {isActive && (
                                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-[85%] w-14 h-9 bg-orange-500/10 rounded-full" />
                            )}
                            <Icon 
                                size={22} 
                                className={`relative z-10 transition-colors duration-300 ${isActive ? 'text-orange-500' : 'text-slate-400 dark:text-zinc-500 group-hover:text-slate-600 dark:group-hover:text-zinc-300'}`} 
                                strokeWidth={isActive ? 2.5 : 2}
                            />
                            <span 
                                className={`relative z-10 text-[10px] transition-colors duration-300 ${isActive ? 'font-black text-orange-600 dark:text-orange-400' : 'font-bold text-slate-400 dark:text-zinc-500 group-hover:text-slate-600 dark:group-hover:text-zinc-300'}`}
                            >
                                {item.label}
                            </span>
                        </Link>
                    );
                })}
            </div>
        </div>
    );
};

export default BottomNavBar;
