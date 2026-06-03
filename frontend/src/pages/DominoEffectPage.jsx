import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Zap, Wallet, Link, ChevronDown, ChevronUp, ArrowDown, CheckCircle, ExternalLink } from 'lucide-react';

const DOMINO_CHAIN = [
    {
        id: 'ration_card',
        step: 1,
        schemeName: 'Step 1: BPL Ration Card',
        ministry: 'Dept. of Food & Public Distribution',
        benefit: 7200,
        docs: ['Aadhaar Card', 'Income Proof (< ₹1L)', 'Domicile Certificate'],
        unlockReason: 'A BPL Ration Card is the master key — it is accepted as proof of income AND domicile by 80% of Central welfare programs.',
        unlocksSchemes: ['PM-JAY', 'PM Awas Yojana', 'PMUY'],
        isFirst: true,
    },
    {
        id: 'pmjay',
        step: 2,
        schemeName: 'Step 2: PM-JAY Health Insurance',
        ministry: 'Ministry of Health',
        benefit: 50000,
        docs: ['BPL Ration Card ✓ (auto-qualified)', 'Aadhaar Card'],
        unlockReason: 'BPL Card auto-qualifies your household. Registering for PM-JAY adds you to the SECC database — which unlocks PM Awas Yojana.',
        unlocksSchemes: ['PM Awas Yojana', 'NFBS'],
        isFirst: false,
    },
    {
        id: 'pmay',
        step: 3,
        schemeName: 'Step 3: PM Awas Yojana (Housing)',
        ministry: 'Ministry of Housing',
        benefit: 120000,
        docs: ['SECC Registration ✓ (auto-qualified via PM-JAY)', 'Aadhaar', 'Bank Account'],
        unlockReason: 'PMAY approval generates a Government Project beneficiary ID — that ID is accepted as residency proof by PM-KISAN and PMUY.',
        unlocksSchemes: ['PM-KISAN', 'PMUY Gas'],
        isFirst: false,
    },
    {
        id: 'pmkisan',
        step: 4,
        schemeName: 'Step 4: PM-KISAN (If Farmer)',
        ministry: 'Ministry of Agriculture',
        benefit: 6000,
        docs: ['Land Records', 'Aadhaar ✓', 'PMAY ID ✓ (residency auto-proven)'],
        unlockReason: 'Land record submission for PM-KISAN simultaneously registers you for State-level Kisan Credit Card schemes.',
        unlocksSchemes: ['Kisan Credit Card', 'PMFBY Insurance'],
        isFirst: false,
    },
];

const totalValue = DOMINO_CHAIN.reduce((sum, s) => sum + s.benefit, 0);

const DominoEffectPage = () => {
    const navigate = useNavigate();
    const [expanded, setExpanded] = useState(0);

    return (
        <div className="flex-1 w-full max-w-2xl mx-auto py-6 px-4">

            {/* Back */}
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-xs font-bold text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-white uppercase tracking-widest transition-colors mb-6"
            >
                <ArrowLeft size={16} /> Back
            </button>

            {/* Hero banner */}
            <div className="relative rounded-[2rem] overflow-hidden mb-8 border border-orange-500/20">
                <div className="h-1 w-full bg-gradient-to-r from-orange-600 via-rose-500 to-amber-400" />
                <div className="bg-[#0D0D1A] p-6 sm:p-8">
                    <div className="flex items-center gap-4 mb-5">
                        <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-orange-600 to-rose-500 flex items-center justify-center shrink-0">
                            <Zap size={24} className="text-white" />
                        </div>
                        <div>
                            <p className="text-orange-400 text-[10px] font-black tracking-[0.2em] uppercase">AI Strategy Engine</p>
                            <h1 className="text-white text-2xl font-black tracking-tight">Domino Effect</h1>
                            <p className="text-zinc-400 text-xs">Scheme Stacking Chain</p>
                        </div>
                    </div>
                    <p className="text-zinc-300 text-sm leading-relaxed font-medium mb-6">
                        One document unlocks everything. Apply for the foundational scheme first — 
                        it auto-satisfies 80% of the eligibility criteria for all schemes after it.
                        Follow the chain in order for maximum household payout.
                    </p>
                    {/* Total value chip */}
                    <div className="inline-flex items-center gap-2 px-4 py-3 bg-orange-500/10 border border-orange-500/30 rounded-xl">
                        <Wallet size={16} className="text-orange-400" />
                        <span className="text-orange-300 font-black text-sm">
                            Total Household Unlock: ₹{totalValue.toLocaleString('en-IN')}/year
                        </span>
                    </div>
                </div>
            </div>

            {/* Step Cards */}
            <div className="space-y-0">
                {DOMINO_CHAIN.map((step, idx) => (
                    <div key={step.id}>
                        <div
                            className={`relative rounded-2xl border transition-all duration-300 ${
                                step.isFirst
                                    ? 'border-orange-500/40 bg-[#1A1A3A]'
                                    : 'border-white/10 bg-[#141427]'
                            }`}
                        >
                            {/* Header */}
                            <button
                                onClick={() => setExpanded(expanded === idx ? -1 : idx)}
                                className="w-full text-left p-5 flex items-center gap-4"
                            >
                                {/* Step badge */}
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 font-black text-lg text-white ${
                                    step.isFirst
                                        ? 'bg-gradient-to-br from-orange-600 to-rose-500'
                                        : 'bg-white/10'
                                }`}>
                                    {step.step}
                                </div>
                                <div className="flex-1 min-w-0">
                                    {step.isFirst && (
                                        <p className="text-orange-400 text-[9px] font-black tracking-[0.2em] uppercase mb-0.5">Start Here</p>
                                    )}
                                    <p className="text-white font-bold text-sm truncate">{step.schemeName}</p>
                                    <p className="text-zinc-500 text-xs">{step.ministry}</p>
                                </div>
                                <div className="flex items-center gap-3 shrink-0">
                                    <span className="px-2.5 py-1 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-black rounded-lg">
                                        ₹{step.benefit.toLocaleString('en-IN')}
                                    </span>
                                    {expanded === idx
                                        ? <ChevronUp size={16} className="text-zinc-500" />
                                        : <ChevronDown size={16} className="text-zinc-500" />
                                    }
                                </div>
                            </button>

                            {/* Expanded */}
                            {expanded === idx && (
                                <div className="px-5 pb-5 space-y-4 border-t border-white/5 pt-4">
                                    {/* Unlock reason */}
                                    <div className="flex gap-3 p-3 bg-emerald-500/5 border border-emerald-500/15 rounded-xl">
                                        <Link size={16} className="text-emerald-400 shrink-0 mt-0.5" />
                                        <p className="text-emerald-400 text-xs font-medium leading-relaxed">{step.unlockReason}</p>
                                    </div>
                                    {/* Documents */}
                                    <div>
                                        <p className="text-zinc-500 text-[10px] font-bold tracking-widest uppercase mb-2">Required Documents</p>
                                        <div className="space-y-1.5">
                                            {step.docs.map((doc, i) => (
                                                <div key={i} className="flex items-center gap-2">
                                                    <CheckCircle size={13} className="text-orange-500 shrink-0" />
                                                    <span className="text-zinc-300 text-xs">{doc}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                    {/* Unlocks */}
                                    {step.unlocksSchemes.length > 0 && (
                                        <div>
                                            <p className="text-zinc-500 text-[10px] font-bold tracking-widest uppercase mb-2">This Unlocks →</p>
                                            <div className="flex flex-wrap gap-2">
                                                {step.unlocksSchemes.map((s, i) => (
                                                    <span key={i} className="px-2.5 py-1 bg-orange-500/10 border border-orange-500/20 text-orange-400 text-xs font-bold rounded-lg">
                                                        {s}
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                    {/* Apply CTA */}
                                    <a
                                        href="https://myscheme.gov.in"
                                        target="_blank"
                                        rel="noreferrer"
                                        className="w-full flex items-center justify-center gap-2 py-3 bg-orange-600 hover:bg-orange-700 text-white rounded-xl text-sm font-bold transition-all"
                                    >
                                        Apply for {step.schemeName.split(':')[1]?.trim() || step.schemeName}
                                        <ExternalLink size={14} />
                                    </a>
                                </div>
                            )}
                        </div>

                        {/* Connector */}
                        {idx < DOMINO_CHAIN.length - 1 && (
                            <div className="flex flex-col items-center py-2">
                                <div className="w-0.5 h-3 bg-orange-500/30" />
                                <ArrowDown size={18} className="text-orange-500/60" />
                            </div>
                        )}
                    </div>
                ))}
            </div>

        </div>
    );
};

export default DominoEffectPage;
