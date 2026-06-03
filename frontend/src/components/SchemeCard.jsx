import { Link } from 'react-router-dom';
import StatusBadge from './StatusBadge';

export const SchemeCard = ({ scheme, showScore = false, basePath = '/scheme', hideStatus = false }) => {
    const getCategoryIcon = (cat) => {
        if (!cat) return "🏛️";
        if (cat.includes("Health")) return "🏥";
        if (cat.includes("Education")) return "📚";
        if (cat.includes("Agriculture")) return "🌱";
        if (cat.includes("Business")) return "💼";
        if (cat.includes("Housing")) return "🏠";
        if (cat.includes("Social")) return "🤝";
        return "🏛️";
    };

    return (
        <div className="pro-card rounded-3xl p-6 sm:p-8 flex flex-col h-full hover:border-orange-500/30 hover:shadow-[0_8px_30px_rgba(255,100,0,0.12)] transition-all group overflow-hidden relative border border-slate-200 dark:border-white/5 bg-white dark:bg-zinc-900 shadow-2xl shadow-black/5 dark:shadow-black/40">
            {/* Subtle glow layer behind the card */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-orange-500/5 blur-[50px] rounded-full pointer-events-none group-hover:bg-orange-500/10 transition-colors"></div>

            <div className="flex justify-between items-start mb-5 gap-4 relative z-10">
                <div className="flex flex-col gap-3">
                    <span className="inline-flex w-max items-center gap-1.5 px-3 py-1.5 rounded-lg text-[10px] font-bold uppercase tracking-wider bg-slate-50 dark:bg-[#0B0E14] text-slate-600 dark:text-zinc-300 border border-slate-200 dark:border-white/5">
                        {getCategoryIcon(scheme.scheme_category)} <span className="text-slate-700 dark:text-zinc-100">{scheme.scheme_category || "General"}</span>
                    </span>
                    <h3 className="font-extrabold text-slate-900 dark:text-white tracking-tight text-lg leading-snug group-hover:text-orange-500 transition-colors line-clamp-3">{scheme.scheme_name}</h3>
                </div>
                {!hideStatus && (
                    <div className="shrink-0 mt-1">
                        <StatusBadge status={scheme.status} />
                    </div>
                )}
            </div>

            {/* Micro-Pill Metadata */}
            <div className="flex flex-wrap items-center gap-2 mb-6 relative z-10">
                <span className="px-2.5 py-1 rounded bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 text-[9px] font-black uppercase tracking-widest text-slate-500 dark:text-zinc-400">
                    {scheme.state_applicable || 'All India'}
                </span>
                <span className="px-2.5 py-1 rounded bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 text-[9px] font-black uppercase tracking-widest text-slate-500 dark:text-zinc-400">
                    v{scheme.version || 1}
                </span>
                {!!scheme.max_financial_value_inr && scheme.max_financial_value_inr > 0 && (
                    <span className="px-2.5 py-1 rounded bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-500/20 text-[9px] font-black uppercase tracking-widest text-emerald-600 dark:text-emerald-400">
                        Max: ₹{scheme.max_financial_value_inr.toLocaleString('en-IN')}
                    </span>
                )}
            </div>

            <div className={`mt-auto pt-2 relative z-10 mt-8`}>
                <Link to={`${basePath}/${scheme.scheme_id}`} state={{ resultData: scheme }} className="block w-full text-center bg-transparent border border-orange-500/20 text-orange-600 dark:text-orange-500 hover:bg-orange-600 hover:text-white hover:border-transparent font-bold py-3.5 rounded-xl transition-all tracking-wide">
                    View Complete Details
                </Link>
            </div>
        </div>
    );
};
