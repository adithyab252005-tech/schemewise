import { useState } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import { Shield, Sparkles, AlertCircle, ArrowRight, ArrowLeft, CheckCircle2 } from 'lucide-react';

const EligibilityResultsPage = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const resultsData = location.state?.results; // Should be array from evaluate logic

    const [filter, setFilter] = useState('ALL'); // ALL, ELIGIBLE, PARTIAL

    if (!resultsData) return <Navigate to="/dashboard" replace />;

    const eligible = resultsData.filter(s => s.status === 'Eligible');
    const partial = resultsData.filter(s => s.status === 'Partially Eligible');
    const totalSchemesChecked = resultsData.length;

    const navTabs = [
        { id: 'ALL', label: `All Processed (${totalSchemesChecked})` },
        { id: 'ELIGIBLE', label: `Perfect Match (${eligible.length})` },
        { id: 'PARTIAL', label: `Missing Docs (${partial.length})` }
    ];

    const getFiltered = () => {
        if (filter === 'ELIGIBLE') return eligible;
        if (filter === 'PARTIAL') return partial;
        return resultsData;
    };

    return (
        <div className="flex-1 w-full max-w-5xl mx-auto py-8 px-4 sm:px-6">
            
            <button onClick={() => navigate('/dashboard')} className="mb-6 flex items-center gap-2 text-sm font-bold text-slate-500 dark:text-zinc-400 hover:text-orange-500 transition-colors">
                <ArrowLeft size={16} /> Return to Dashboard
            </button>

            {/* Results Header */}
            <div className="pro-card rounded-3xl p-8 border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 mb-6 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative overflow-hidden">
                <div className="relative z-10">
                    <div className="flex items-center gap-3 mb-3">
                        <span className="bg-emerald-100 text-emerald-700 border border-emerald-200 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-widest flex items-center gap-1.5">
                            <Sparkles size={14} /> Evaluation Complete
                        </span>
                    </div>
                    <h1 className="text-3xl font-black text-slate-900 dark:text-white tracking-tighter mb-2 tracking-tight">AI Engine Results mapped {eligible.length} guaranteed schemes.</h1>
                    <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium max-w-lg leading-relaxed">Cross-referenced against 500+ central and state schemes instantly using your verified demographic array.</p>
                </div>

                <div className="flex gap-4 shrink-0 relative z-10 w-full md:w-auto overflow-x-auto pb-2 md:pb-0">
                    <div className="bg-emerald-50 rounded-2xl p-4 border border-emerald-100 min-w-[120px] shrink-0 text-center">
                        <span className="block text-3xl font-black text-emerald-600 mb-1">{eligible.length}</span>
                        <span className="text-[10px] font-bold text-emerald-700 uppercase tracking-widest">Matched</span>
                    </div>
                    <div className="bg-amber-50 rounded-2xl p-4 border border-amber-100 min-w-[120px] shrink-0 text-center">
                        <span className="block text-3xl font-black text-amber-600 mb-1">{partial.length}</span>
                        <span className="text-[10px] font-bold text-amber-700 uppercase tracking-widest">Fixable</span>
                    </div>
                </div>
            </div>

            {/* Filters */}
            <div className="flex gap-2 overflow-x-auto custom-scrollbar pb-2 mb-6">
                {navTabs.map(tab => (
                    <button 
                        key={tab.id}
                        onClick={() => setFilter(tab.id)}
                        className={`px-5 py-2.5 rounded-full text-sm font-bold whitespace-nowrap transition-colors ${filter === tab.id ? 'bg-orange-600 text-slate-900 dark:text-white shadow-2xl shadow-black/5 dark:shadow-black/40' : 'pro-card text-slate-600 dark:text-zinc-300 border border-slate-200 dark:border-white/5 hover:border-slate-200 dark:border-white/5 focus:bg-orange-600 focus:text-slate-900 dark:text-white'}`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Results Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {getFiltered().map((scheme, idx) => (
                    <div 
                        key={idx} 
                        onClick={() => navigate(`/scheme/${scheme.scheme_id}`, { state: { resultData: scheme } })}
                        className="pro-card rounded-3xl p-6 border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 flex flex-col justify-between hover:border-slate-200 dark:border-zinc-800/30 hover:shadow-md cursor-pointer transition-all group"
                    >
                        <div>
                            <div className="flex justify-between items-start mb-4 gap-4">
                                <span className={`px-2.5 py-1 text-[10px] font-bold uppercase tracking-widest rounded-md border ${
                                    scheme.status === 'Eligible' ? 'bg-emerald-50 text-emerald-600 border-emerald-200' : 
                                    scheme.status === 'Partially Eligible' ? 'bg-amber-50 text-amber-600 border-amber-200' : 
                                    'bg-rose-50 text-rose-600 border-rose-200'
                                }`}>
                                    {scheme.status}
                                </span>
                                {scheme.status === 'Eligible' ? (
                                    <CheckCircle2 size={24} className="text-emerald-500 shrink-0" />
                                ) : scheme.status === 'Partially Eligible' ? (
                                    <AlertCircle size={24} className="text-amber-500 shrink-0" />
                                ) : <Shield size={24} className="text-slate-500 dark:text-zinc-400 shrink-0" />}
                            </div>

                            <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight mb-2 leading-snug group-hover:text-orange-500 transition-colors">{scheme.scheme_name}</h3>
                            <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium line-clamp-2 mb-4 leading-relaxed tracking-wide">
                                Type: {scheme.scheme_type} | Target: {scheme.category}
                            </p>
                        </div>
                        
                        <div className="pt-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between">
                            <span className="text-orange-500 font-bold text-[13px] hover:underline flex items-center gap-1">
                                View Details <ArrowRight size={14} />
                            </span>
                            {scheme.status === 'Partially Eligible' && scheme.missing_conditions?.length > 0 && (
                                <span className="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-1 rounded truncate max-w-[150px]">
                                    Requires: {scheme.missing_conditions[0]}
                                </span>
                            )}
                        </div>
                    </div>
                ))}
            </div>

        </div>
    );
};

export default EligibilityResultsPage;
