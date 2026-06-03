import { useState } from 'react';
import { Users, HelpCircle, Server, CheckCircle2, ArrowLeft } from 'lucide-react';
import { useProfile } from '../context/ProfileContext';
import { useNavigate } from 'react-router-dom';

const ComboBenefitsPage = () => {
    const { getActiveProfile } = useProfile();
    const profile = getActiveProfile();
    const navigate = useNavigate();

    const [extractingId, setExtractingId] = useState(null);
    const [extracted, setExtracted] = useState({});

    // Mock combo potentials based on household
    const combos = [
        { id: 1, title: "Family Healthcare Alliance (PM-JAY)", tag: "Medical", match: "98%", members: 3, amount: "₹5,00,000", glow: "text-rose-500", border: "border-rose-500/20" },
        { id: 2, title: "Agricultural Household Subsidy", tag: "Farming", match: "85%", members: 2, amount: "₹12,000 / yr", glow: "text-emerald-500", border: "border-emerald-500/20" },
        { id: 3, title: "First-Generation Scholar Sync", tag: "Education", match: "70%", members: 2, amount: "Tuition Waiver", glow: "text-indigo-500", border: "border-indigo-500/20" },
    ];

    const handleExtract = (id) => {
        setExtractingId(id);
        setTimeout(() => {
            setExtracted(prev => ({ ...prev, [id]: true }));
            setExtractingId(null);
        }, 2000);
    };

    return (
        <div className="flex flex-col gap-6 max-w-7xl mx-auto pb-12 px-4 md:px-8 pt-8 min-h-full animate-fade-in relative z-10 w-full">
            
            <button onClick={() => navigate(-1)} className="mb-0 flex items-center gap-2 text-sm font-bold text-slate-500 dark:text-zinc-400 hover:text-orange-500 transition-colors w-fit">
                <ArrowLeft size={16} /> Return to Profile
            </button>

            {/* God-Tier Header */}
            <div className="w-full bg-[#0B0E14] border border-white/5 shadow-2xl p-8 sm:p-10 rounded-3xl flex flex-col justify-between relative overflow-hidden group mb-4">
                 <img src="/landing_hero_premium.png" alt="Geometric Architectural Data" className="absolute inset-0 w-full h-full object-cover opacity-[0.25] mix-blend-screen group-hover:scale-105 transition-transform duration-[5s] ease-out pointer-events-none" />
                 <div className="absolute inset-0 bg-gradient-to-t from-[#0B0E14] via-[#0B0E14]/70 to-transparent pointer-events-none"></div>

                <div className="relative z-10 flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6">
                    <div>
                        <div className="flex items-center gap-3 mb-4">
                            <div className="px-3 py-1.5 rounded-md bg-amber-500/10 border border-amber-500/20 flex items-center gap-2 shadow-[0_0_15px_rgba(245,158,11,0.1)]">
                                <Users size={12} className="text-amber-500" />
                                <span className="text-[10px] font-black text-amber-500 uppercase tracking-widest leading-none">Family Link Active</span>
                            </div>
                        </div>
                        <h1 className="text-4xl sm:text-5xl font-black tracking-tighter leading-tight text-white drop-shadow-md">
                            Household <span className="text-amber-500">Benefits</span>
                        </h1>
                        <p className="font-medium text-white/60 text-sm mt-3 max-w-lg leading-relaxed">
                            Link your family members to unlock combined household schemes like Family Healthcare and Ration Cards.
                        </p>
                    </div>
                </div>
            </div>

            {/* List of Combos */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {combos.map((combo) => (
                    <div key={combo.id} className={`pro-card p-6 rounded-3xl border transition-all duration-300 shadow-2xl flex flex-col justify-between group 
                        ${extracted[combo.id] ? 'border-emerald-500/50 bg-emerald-50/10 dark:bg-emerald-900/10' : `${combo.border} hover:border-amber-500/50`}
                    `}>
                        <div>
                            <div className="flex justify-between items-start mb-4">
                                <span className={`text-[10px] font-bold uppercase tracking-widest ${extracted[combo.id] ? 'text-emerald-500' : combo.glow}`}>{combo.tag}</span>
                                <div className="px-2 py-1 rounded bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 text-xs font-bold text-slate-700 dark:text-zinc-300 shadow-sm">
                                    {combo.match} Match
                                </div>
                            </div>
                            <h3 className="text-xl font-bold text-slate-900 dark:text-white tracking-tight mb-2 leading-tight">{combo.title}</h3>
                            <p className="text-sm text-slate-500 dark:text-zinc-400 font-medium mb-6">Requires data synchronization from {combo.members} household members.</p>
                        </div>
                        
                        <div className="border-t border-slate-200 dark:border-white/5 pt-4 flex items-center justify-between">
                            <div className="flex flex-col">
                                <span className="text-[10px] font-bold text-slate-400 dark:text-zinc-500 uppercase tracking-wider mb-0.5">Yield</span>
                                <span className="text-base font-black text-slate-900 dark:text-white">{combo.amount}</span>
                            </div>
                            
                            <button 
                                disabled={extractingId === combo.id || extracted[combo.id]}
                                onClick={() => handleExtract(combo.id)}
                                className={`px-4 py-2 rounded-xl text-sm font-bold flex items-center gap-2 transition-all cursor-pointer
                                    ${extracted[combo.id] 
                                        ? 'bg-emerald-500 text-white cursor-default' 
                                        : extractingId === combo.id 
                                            ? 'bg-zinc-200 text-zinc-500 cursor-wait dark:bg-zinc-800'
                                            : 'bg-amber-100 dark:bg-amber-500/10 text-amber-600 dark:text-amber-500 hover:bg-amber-200 dark:hover:bg-amber-500/20'
                                    }
                                `}>
                                {extractingId === combo.id && <Server size={14} className="animate-pulse" />}
                                {extracted[combo.id] && <CheckCircle2 size={14} />}
                                {extracted[combo.id] 
                                    ? 'Extracted' 
                                    : extractingId === combo.id 
                                        ? 'Syncing...' 
                                        : 'Extract'}
                            </button>
                        </div>
                    </div>
                ))}
            </div>
            
            {/* Guide */}
            <div className="pro-card p-6 sm:p-8 rounded-3xl border border-white/5 shadow-xl mt-4 bg-slate-50 dark:bg-[#11151C] flex items-start gap-4">
                <div className="w-12 h-12 rounded-2xl bg-white dark:bg-[#0B0E14] border border-slate-200 dark:border-white/5 flex items-center justify-center shrink-0">
                    <HelpCircle size={24} className="text-slate-500 dark:text-zinc-400" />
                </div>
                <div>
                    <h4 className="text-sm font-bold text-slate-900 dark:text-white mb-1">How do household benefits work?</h4>
                    <p className="text-sm text-slate-500 dark:text-zinc-400 leading-relaxed font-medium">Certain government schemas (like ration frameworks and vast health coverage nets) strictly require linked household profiles. By ensuring all family members are actively added to your <strong>Profile Network</strong>, the engine can find special packages meant for your entire family.</p>
                </div>
            </div>

        </div>
    );
};

export default ComboBenefitsPage;
