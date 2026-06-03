import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useSchemes } from '../hooks/useSchemes';
import { useProfile } from '../context/ProfileContext';
import { Activity, ShieldCheck, ExternalLink, Inbox, Trash2 } from 'lucide-react';

const SavedSchemesPage = () => {
    const { fetchSavedSchemes, unsaveUserScheme } = useSchemes();
    const { getActiveProfile } = useProfile();
    const [savedSchemes, setSavedSchemes] = useState([]);
    const [loading, setLoading] = useState(true);

    const profile = getActiveProfile();

    useEffect(() => {
        const load = async () => { 
            if (profile?.id) {
                const data = await fetchSavedSchemes(profile.id); 
                if (data) setSavedSchemes(data); 
            }
            setLoading(false); 
        };
        load();
    }, [fetchSavedSchemes, profile?.id]);

    const handleUnsave = async (schemeId) => {
        if (!profile?.id) return;
        // Optimistic UI update
        setSavedSchemes(prev => prev.filter(s => s.scheme_id !== schemeId));
        await unsaveUserScheme(schemeId, profile.id);
    };

    return (
        <div className="flex-1 w-full max-w-5xl mx-auto px-6 py-10">
            {/* Minimalist Header */}
            <div className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-6 border-b border-slate-200 dark:border-slate-200 dark:border-zinc-800/80 pb-6">
                <div>
                    <h1 className="text-2xl font-semibold text-slate-800 dark:text-zinc-100 tracking-tight flex items-center gap-2 mb-1.5">
                        <Activity className="text-orange-500" size={24} /> 
                        Monitoring Hub
                    </h1>
                    <p className="text-slate-500 dark:text-zinc-400 text-sm">Your saved schemes are actively audited and monitored for changes.</p>
                </div>
                
                {/* Refined Stats Block (Linear Style) */}
                <div className="flex items-center gap-6 px-5 py-3 pro-card rounded-xl">
                    <div className="flex flex-col">
                        <span className="text-lg font-bold text-slate-800 dark:text-zinc-100 leading-tight">{savedSchemes.length}</span>
                        <span className="text-[10px] font-medium text-slate-500 dark:text-zinc-500 uppercase tracking-widest">Tracking</span>
                    </div>
                    <div className="w-px h-8 bg-zinc-800"></div>
                    <div className="flex flex-col items-start gap-1 justify-center">
                        <div className="flex items-center gap-1.5">
                            <span className="relative flex h-2 w-2 shadow-[0_0_8px_rgba(16,185,129,0.8)]">
                              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                            </span>
                            <span className="text-xs font-semibold text-emerald-400 leading-none">Active</span>
                        </div>
                        <span className="text-[10px] font-medium text-slate-500 dark:text-zinc-500 uppercase tracking-widest leading-none">Agents</span>
                    </div>
                </div>
            </div>

            {loading ? (
                <div className="flex flex-col items-center justify-center py-32 gap-4">
                    <div className="w-6 h-6 border-2 border-slate-200 dark:border-zinc-800 border-t-orange-500 rounded-full animate-spin"></div>
                    <span className="text-sm font-medium text-slate-500 dark:text-zinc-500">Syncing database...</span>
                </div>
            ) : savedSchemes.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-32">
                    <div className="w-16 h-16 rounded-2xl pro-card flex items-center justify-center mb-5 text-zinc-600">
                        <Inbox size={32} strokeWidth={1.5} />
                    </div>
                    <h2 className="text-lg font-semibold text-slate-700 dark:text-zinc-200 mb-1.5">No schemes tracked</h2>
                    <p className="text-slate-500 dark:text-zinc-500 text-sm max-w-sm text-center mb-6">You haven't added any schemes to your monitoring hub yet.</p>
                    <Link to="/explore" className="px-5 py-2.5 bg-zinc-100 hover:bg-white text-zinc-900 font-semibold rounded-lg text-sm transition-colors shadow-[0_0_15px_rgba(255,255,255,0.1)]">
                        Explore Directory
                    </Link>
                </div>
            ) : (
                <div className="flex flex-col gap-3">
                    {/* List Header */}
                    <div className="grid grid-cols-12 gap-4 px-5 py-2 text-[11px] font-semibold text-slate-500 dark:text-zinc-500 uppercase tracking-widest border-b border-slate-300 dark:border-zinc-900">
                        <div className="col-span-7">Scheme Name</div>
                        <div className="col-span-2 hidden md:block">Category</div>
                        <div className="col-span-2 hidden md:block">Jurisdiction</div>
                        <div className="col-span-3 md:col-span-1 text-right">Action</div>
                    </div>

                    {/* Vercel-style List Rows */}
                    {savedSchemes.map(item => (
                        <div key={item.saved_id} className="grid grid-cols-12 gap-4 items-center px-6 py-5 pro-card hover:scale-[1.01] shadow-sm hover:shadow-md rounded-2xl transition-all duration-300 group">
                            
                            <div className="col-span-9 md:col-span-7 flex flex-col pr-4">
                                <h3 className="text-sm font-semibold text-slate-700 dark:text-zinc-200 group-hover:text-orange-400 transition-colors leading-relaxed line-clamp-2">
                                    {item.scheme_name}
                                </h3>
                                <div className="flex items-center gap-3 mt-1.5">
                                    <span className="text-[11px] font-medium text-slate-500 dark:text-zinc-500">
                                        Added {new Date(item.saved_at).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                                    </span>
                                    <div className="flex items-center gap-1.5 bg-emerald-500/10 text-emerald-400 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border border-emerald-500/20">
                                        <div className="w-1.5 h-1.5 rounded-full bg-emerald-500"></div> Monitoring
                                    </div>
                                </div>
                            </div>
                            
                            <div className="col-span-2 hidden md:flex items-center">
                                <span className="text-[11px] font-medium text-slate-500 dark:text-zinc-400 px-2 py-1 bg-white dark:bg-zinc-900 rounded border border-slate-200 dark:border-zinc-800 whitespace-nowrap">
                                    {item.scheme_category || "General"}
                                </span>
                            </div>

                            <div className="col-span-2 hidden md:flex items-center">
                                <span className="text-[12px] font-medium text-slate-500 dark:text-zinc-500">
                                    {item.scheme_type === 'Central' || item.state_applicable === 'ALL' ? 'Central' : item.state_applicable}
                                </span>
                            </div>

                            <div className="col-span-3 md:col-span-1 flex justify-end gap-1">
                                <Link to={`/scheme/${item.scheme_id}`} className="text-slate-500 dark:text-zinc-500 hover:text-orange-500 p-2 hover:bg-slate-200 dark:hover:bg-zinc-800 rounded-lg transition-colors" title="View Details">
                                    <ExternalLink size={18} strokeWidth={2} />
                                </Link>
                                <button onClick={() => handleUnsave(item.scheme_id)} className="text-slate-400 dark:text-zinc-600 hover:text-red-500 p-2 hover:bg-slate-200 dark:hover:bg-zinc-800 rounded-lg transition-colors" title="Remove Scheme">
                                    <Trash2 size={18} strokeWidth={2} />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default SavedSchemesPage;
