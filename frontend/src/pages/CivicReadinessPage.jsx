import { useState } from 'react';
import { ShieldCheck, Target, CheckCircle2, Circle, AlertCircle, FileText, Info } from 'lucide-react';
import { useProfile } from '../context/ProfileContext';

const CivicReadinessPage = () => {
    const { getActiveProfile, updateProfile } = useProfile();
    const profile = getActiveProfile();

    const [isVerifying, setIsVerifying] = useState(false);
    const [verificationStep, setVerificationStep] = useState(0);

    // Dynamic Logic: Generate exactly what this specific user profile needs
    const getDynamicDocs = () => {
        let docs = [
            { name: 'Aadhaar Card', required: true, reason: 'Primary Identity Verification', vector: 'Identity' },
            { name: 'PAN Card', required: true, reason: 'Financial & Tax Compliance', vector: 'Identity' },
            { name: 'Domicile Certificate', required: true, reason: 'State-level Scheme Entitlement', vector: 'Demographic' },
            { name: 'Income Certificate', required: true, reason: 'Socio-Economic Verification', vector: 'SocioEconomic' }
        ];

        if (profile?.category && profile.category !== 'General') {
            docs.push({ name: 'Caste Certificate', required: true, reason: `Verifies ${profile.category} Category Entitlements`, vector: 'Demographic' });
        }

        if (profile?.isBPL === 'Yes') {
            docs.push({ name: 'Ration Card (BPL)', required: true, reason: 'Verifies Below Poverty Line Status', vector: 'SocioEconomic' });
        } else {
            docs.push({ name: 'Ration Card', required: false, reason: 'Optional Family Proof', vector: 'SocioEconomic' });
        }

        if (profile?.isDifferentlyAbled === 'Yes') {
            docs.push({ name: 'Disability Certificate', required: true, reason: 'Special Exemptions & Healthcare', vector: 'Special' });
        }
        return docs;
    };

    const dynamicDocs = getDynamicDocs();
    const currentDocs = profile?.hasDocuments || [];

    // Filter dynamic required lists per vector
    const checkVector = (vectorName) => {
        const requiredForVector = dynamicDocs.filter(d => d.vector === vectorName && d.required).map(d => d.name);
        if (requiredForVector.length === 0) return { verified: true, total: 0, checked: 0 };
        const checked = requiredForVector.filter(doc => currentDocs.includes(doc)).length;
        return {
            verified: checked === requiredForVector.length,
            total: requiredForVector.length,
            checked: checked,
            score: Math.round((checked / requiredForVector.length) * 100)
        };
    };

    const identityCheck = checkVector('Identity');
    const socioCheck = checkVector('SocioEconomic');
    const demoCheck = checkVector('Demographic');
    const specialCheck = checkVector('Special');

    const stats = [
        { 
            label: "Primary Identity Core", 
            status: identityCheck.verified ? "VERIFIED" : "ACTION REQUIRED", 
            color: identityCheck.verified ? "emerald" : "rose", 
            score: identityCheck.score
        },
        { 
            label: "Socio-Economic Evaluation", 
            status: socioCheck.verified ? "VERIFIED" : "PENDING SYNC", 
            color: socioCheck.verified ? "emerald" : "amber", 
            score: socioCheck.score
        },
        { 
            label: "Demographic Entitlement", 
            status: demoCheck.verified ? "VERIFIED" : "AWAITING INPUT", 
            color: demoCheck.verified ? "emerald" : "rose", 
            score: demoCheck.score
        },
    ];

    if (profile?.isDifferentlyAbled === 'Yes') {
        stats.push({
            label: "Special Exemptions", 
            status: specialCheck.verified ? "VERIFIED" : "MISSING CERT", 
            color: specialCheck.verified ? "emerald" : "amber", 
            score: specialCheck.score
        });
    }

    const toggleChecklist = async (docName) => {
        if (!profile) return;
        const newDocs = currentDocs.includes(docName)
            ? currentDocs.filter(d => d !== docName)
            : [...currentDocs, docName];
        await updateProfile(profile.id, { hasDocuments: newDocs });
    };

    const handleVerification = () => {
        setIsVerifying(true);
        setVerificationStep(1);
        setTimeout(() => {
            setVerificationStep(2);
            setIsVerifying(false);
        }, 2000);
    };

    const totalRequired = dynamicDocs.filter(d => d.required).length;
    const totalFilled = dynamicDocs.filter(d => d.required && currentDocs.includes(d.name)).length;
    const overallIndexRaw = totalRequired > 0 ? Math.round((totalFilled / totalRequired) * 100) : 100;
    const overallIndex = verificationStep === 2 ? Math.min(100, overallIndexRaw + 5) : overallIndexRaw;

    return (
        <div className="flex flex-col gap-6 max-w-7xl mx-auto pb-24 px-4 md:px-8 pt-6 min-h-full animate-fade-in relative z-10 w-full">
            
            <div className="w-full pro-card p-6 sm:p-10 flex flex-col justify-between relative overflow-hidden group mb-4 shadow-2xl rounded-3xl backdrop-blur-3xl bg-white/60 dark:bg-black/40 border border-slate-200/50 dark:border-white/10">
                 <img src="/dashboard_hero_abstract.png" alt="Intelligence Core Pattern" className="absolute inset-0 w-full h-full object-cover opacity-[0.2] dark:opacity-[0.4] mix-blend-luminosity md:mix-blend-overlay group-hover:scale-105 transition-transform duration-[5s] ease-out pointer-events-none" />
                 <div className="absolute inset-0 bg-gradient-to-r from-[#06080A]/60 via-[#06080A]/80 to-transparent pointer-events-none dark:block hidden"></div>

                <div className="relative z-10 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6">
                    <div>
                        <div className="flex items-center gap-3 mb-4">
                            <div className="px-3 py-1.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 flex items-center gap-2 shadow-[0_0_15px_rgba(16,185,129,0.1)]">
                                <Target size={12} className="text-emerald-500" />
                                <span className="text-[10px] font-black text-emerald-500 uppercase tracking-widest leading-none">Security Core Verified</span>
                            </div>
                        </div>
                        <h1 className="text-4xl sm:text-5xl font-black tracking-tighter leading-tight text-slate-900 dark:text-white drop-shadow-md">
                            Civic <span className="text-emerald-500 dark:text-emerald-400 opacity-90">Readiness</span>
                        </h1>
                        <p className="font-medium text-slate-700 dark:text-white/60 text-sm mt-3 max-w-md leading-relaxed">
                            Analyze your governmental profile strength. Check off your actual possessed documents to instantly track index metrics.
                        </p>
                    </div>
                    
                    <div className="flex flex-col items-center justify-center p-6 bg-slate-100/50 dark:bg-white/5 backdrop-blur-md rounded-2xl border border-slate-200 dark:border-white/10 shadow-[0_20px_40px_rgba(0,0,0,0.1)] dark:shadow-[0_20px_40px_rgba(0,0,0,0.5)] transition-all">
                        <span className={`text-5xl font-black tracking-tighter drop-shadow-[0_0_15px_rgba(255,255,255,0.4)] mb-1 transition-all duration-1000 ${overallIndex >= 85 ? 'text-emerald-500 dark:text-emerald-400' : overallIndex >= 50 ? 'text-amber-500 dark:text-amber-400' : 'text-rose-500 dark:text-rose-400'}`}>
                            {overallIndex}<span className="text-xl">%</span>
                        </span>
                        <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-slate-500 dark:text-white/60">Overall Index</span>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                
                {/* Left: Required Documentation Checklist */}
                <div className="pro-card p-6 sm:p-8 rounded-3xl shadow-2xl flex flex-col gap-6 relative overflow-hidden bg-white/80 dark:bg-zinc-950/80 border border-slate-200 dark:border-white/5">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <ShieldCheck size={20} className="text-emerald-500" />
                            <h3 className="text-lg font-bold text-slate-900 dark:text-white tracking-tight">Required Documentation</h3>
                        </div>
                        <span className="text-xs font-bold text-slate-500 dark:text-zinc-400">{totalFilled} / {totalRequired} Secured</span>
                    </div>
                        
                    <div className="flex flex-col gap-3 relative z-10">
                        {dynamicDocs.map((doc) => {
                            const isDone = currentDocs.includes(doc.name);
                            return (
                                <button 
                                    key={doc.name} 
                                    onClick={() => toggleChecklist(doc.name)}
                                    className={`flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 p-4 rounded-2xl border transition-all cursor-pointer text-left
                                        ${isDone 
                                            ? 'bg-emerald-50/50 dark:bg-emerald-900/10 border-emerald-200 dark:border-emerald-500/20' 
                                            : 'bg-white dark:bg-zinc-900 border-slate-200 dark:border-white/5 hover:border-slate-300 dark:hover:border-white/10'
                                    }`}
                                >
                                    <div className="flex items-start gap-3">
                                        <div className="mt-1 sm:mt-0">
                                            {isDone ? <CheckCircle2 size={20} className="text-emerald-500 shrink-0" /> : <Circle size={20} className="text-slate-300 dark:text-zinc-600 shrink-0" />}
                                        </div>
                                        <div className="flex flex-col">
                                            <span className={`text-[15px] font-bold ${isDone ? 'text-emerald-700 dark:text-emerald-400 opacity-90' : 'text-slate-700 dark:text-zinc-200'}`}>
                                                {doc.name} {!doc.required && <span className="text-[10px] ml-1 px-1.5 py-0.5 rounded bg-slate-100 dark:bg-zinc-800 text-slate-500 dark:text-zinc-400 uppercase">Optional</span>}
                                            </span>
                                            <span className={`text-xs mt-0.5 ${isDone ? 'text-emerald-600/70 dark:text-emerald-500/60' : 'text-slate-500 dark:text-zinc-500'}`}>{doc.reason}</span>
                                        </div>
                                    </div>
                                </button>
                            );
                        })}
                    </div>
                </div>

                <div className="flex flex-col gap-5 sticky top-6 h-max">
                    {/* Compliance Summary Card */}
                    <div className="pro-card p-6 rounded-2xl border border-slate-100 dark:border-white/5 bg-white dark:bg-[#0B0E14]">
                        <div className="flex items-center gap-2.5 mb-5">
                            <div className="w-7 h-7 rounded-lg bg-slate-50 dark:bg-[#11151C] border border-slate-100 dark:border-white/5 flex items-center justify-center">
                                <FileText size={14} className="text-slate-500 dark:text-zinc-400" />
                            </div>
                            <h3 className="text-sm font-bold text-slate-800 dark:text-white">Document Compliance Summary</h3>
                        </div>

                        <div className="flex flex-col gap-3">
                            {[
                                { label: 'Identity Documents', check: identityCheck, icon: ShieldCheck, color: 'emerald' },
                                { label: 'Socio-Economic Proof', check: socioCheck, icon: Target, color: 'amber' },
                                { label: 'Demographic Certificates', check: demoCheck, icon: Target, color: 'violet' },
                                ...(profile?.isDifferentlyAbled === 'Yes' ? [{ label: 'Special Exemptions', check: specialCheck, icon: AlertCircle, color: 'rose' }] : []),
                            ].map(({ label, check, icon: Icon, color }) => (
                                <div key={label} className="flex items-center justify-between gap-3 py-2.5 border-b border-slate-50 dark:border-white/5 last:border-0">
                                    <div className="flex items-center gap-2.5">
                                        <Icon size={14} className={`text-${color}-500`} />
                                        <span className="text-xs font-semibold text-slate-600 dark:text-zinc-300">{label}</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <span className="text-xs font-black text-slate-800 dark:text-white">{check.checked}/{check.total}</span>
                                        {check.verified 
                                            ? <CheckCircle2 size={14} className="text-emerald-500" />
                                            : <Circle size={14} className="text-slate-300 dark:text-zinc-600" />
                                        }
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* Overall progress bar */}
                        <div className="mt-5 pt-4 border-t border-slate-100 dark:border-white/5">
                            <div className="flex justify-between items-center mb-2">
                                <span className="text-[10px] font-black text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Overall Completeness</span>
                                <span className={`text-sm font-black ${
                                    overallIndex >= 85 ? 'text-emerald-500' : overallIndex >= 50 ? 'text-amber-500' : 'text-rose-500'
                                }`}>{overallIndex}%</span>
                            </div>
                            <div className="h-2 w-full bg-slate-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                                <div 
                                    className={`h-full rounded-full transition-all duration-1000 ease-out ${
                                        overallIndex >= 85 ? 'bg-emerald-500' : overallIndex >= 50 ? 'bg-amber-500' : 'bg-rose-500'
                                    }`}
                                    style={{ width: `${overallIndex}%` }}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Info panel */}
                    <div className="flex gap-3 p-4 rounded-2xl bg-blue-50 dark:bg-blue-500/5 border border-blue-100 dark:border-blue-500/15">
                        <Info size={16} className="text-blue-500 shrink-0 mt-0.5" />
                        <p className="text-xs text-blue-700 dark:text-blue-300 font-medium leading-relaxed">
                            Tick each document you physically possess. Your Civic Readiness Index updates instantly and is used to prioritize your scheme applications during government processing.
                        </p>
                    </div>
                </div>

            </div>
        </div>
    );
};

export default CivicReadinessPage;
