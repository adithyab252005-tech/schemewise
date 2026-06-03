import { useEffect, useState } from 'react';
import { useLocation, useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Share2, Printer, BookmarkPlus, BookmarkCheck, ExternalLink, Info, FileCheck, AlertCircle, Sparkles, CheckCircle2, Shield, Play, Activity, Bot } from 'lucide-react';
import { useProfile } from '../context/ProfileContext';
import { useSchemes } from '../hooks/useSchemes';

const SchemeDetailPage = () => {
    const { id } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const { fetchSchemeDetails, saveUserScheme, loading, error } = useSchemes();
    const { getActiveProfile } = useProfile();
    const [scheme, setScheme] = useState(null);
    const [isSaving, setIsSaving] = useState(false);
    const [savedMsg, setSavedMsg] = useState('');
    const [activeTab, setActiveTab] = useState('overview'); 
    // AI Fetch & Accessibility states
    const [aiExplanation, setAiExplanation] = useState('');
    const [isAiLoading, setIsAiLoading] = useState(false);
    const [isSpeaking, setIsSpeaking] = useState(false);

    const resultData = location.state?.resultData;
    const profile = getActiveProfile();

    useEffect(() => {
        const loadDetails = async () => {
            const data = await fetchSchemeDetails(id);
            if (data) setScheme(data);
        };
        loadDetails();
    }, [id, fetchSchemeDetails]);

    const fetchAiExplanation = async () => {
        setIsAiLoading(true);
        try {
            const basePrompt = `Please provide a highly detailed, step-by-step guide on how and where to apply for the scheme "${scheme.scheme_name}". Include exact document requirements, portal URLs if possible, and specific application procedures. Structure it in a professional, easily actionable format. DO NOT use markdown formatting like asterisks or hashes. Use plain text with simple dashes for lists.`;
            
            const res = await fetch('http://180.235.121.245:8060/api/v1/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    message: basePrompt,
                    history: [],
                    user_profile: profile || {}
                })
            });
            const data = await res.json();
            setAiExplanation(data.reply);
        } catch (e) {
            setAiExplanation("⚠️ Failed to connect to the AI engine. Please try again later.");
        }
        setIsAiLoading(false);
    };

    const toggleSpeech = (text) => {
        if (!window.speechSynthesis) return;
        if (isSpeaking) {
            window.speechSynthesis.cancel();
            setIsSpeaking(false);
        } else {
            const cleanText = text.replace(/[*_`#]/g, '');
            const utterance = new SpeechSynthesisUtterance(cleanText);
            utterance.rate = 0.95;
            utterance.onend = () => setIsSpeaking(false);
            utterance.onerror = () => setIsSpeaking(false);
            
            window.speechSynthesis.speak(utterance);
            setIsSpeaking(true);
        }
    };

    // Cleanup TTS on unmount
    useEffect(() => {
        return () => window.speechSynthesis && window.speechSynthesis.cancel();
    }, []);

    if (loading || !scheme) {
        return (
            <div className="flex-1 flex flex-col items-center justify-center min-h-[500px]">
                <div className="w-8 h-8 rounded-full border-4 border-slate-200 border-t-black dark:border-zinc-800 dark:border-t-white animate-spin"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex-1 flex items-center justify-center min-h-[500px]">
                <div className="text-center p-8 bg-white dark:bg-zinc-900 rounded-3xl border border-rose-200 dark:border-rose-900/30 shadow-sm max-w-sm">
                    <AlertCircle size={48} className="mx-auto text-rose-500 mb-4" />
                    <div className="text-slate-900 dark:text-white text-xl font-bold mb-2 tracking-tight">Failed to load scheme</div>
                    <div className="text-slate-500 dark:text-zinc-400 mb-6 text-sm">{error}</div>
                    <button onClick={() => navigate(-1)} className="px-6 py-2 bg-slate-100 hover:bg-slate-200 dark:bg-zinc-800 dark:hover:bg-zinc-700 text-slate-900 dark:text-white text-sm font-bold rounded-xl transition-all">Go Back</button>
                </div>
            </div>
        );
    }

    let statusBadge = null;
    let iconBadge = <Shield size={24} strokeWidth={2.5} className="text-slate-800 dark:text-zinc-200" />;
    
    if (resultData) {
        if (resultData.status === 'Eligible') {
            statusBadge = <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 rounded-full text-[10px] font-bold uppercase tracking-widest border border-emerald-200/50 dark:border-emerald-500/20"><div className="w-1.5 h-1.5 rounded-full bg-emerald-500"></div> Eligible Match</span>;
            iconBadge = <CheckCircle2 size={24} strokeWidth={2.5} className="text-emerald-500" />;
        } else if (resultData.status === 'Partially Eligible') {
            statusBadge = <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-amber-50 dark:bg-amber-500/10 text-amber-600 dark:text-amber-400 rounded-full text-[10px] font-bold uppercase tracking-widest border border-amber-200/50 dark:border-amber-500/20"><div className="w-1.5 h-1.5 rounded-full bg-amber-500"></div> Partial Match</span>;
            iconBadge = <AlertCircle size={24} strokeWidth={2.5} className="text-amber-500" />;
        } else {
            statusBadge = <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 bg-rose-50 dark:bg-rose-500/10 text-rose-600 dark:text-rose-400 rounded-full text-[10px] font-bold uppercase tracking-widest border border-rose-200/50 dark:border-rose-500/20"><div className="w-1.5 h-1.5 rounded-full bg-rose-500"></div> Not Eligible</span>;
        }
    }

    const handleSave = async () => {
        setIsSaving(true);
        if (profile?.id) {
            const res = await saveUserScheme(id, profile.id);
            if (res && res.message) setSavedMsg("Saved. Deadline Alert Scheduled!");
            else setSavedMsg("Failed");
        } else {
            setSavedMsg("Failed");
        }
        setIsSaving(false);
        setTimeout(() => setSavedMsg(''), 3000);
    };

    const handleShare = () => {
        const msg = `🚨 Found a government scheme on SchemeWise!\n\n*${scheme.scheme_name}*\nCheck eligibility: ${scheme.source_url || "https://myscheme.gov.in"}`;
        if (navigator.share) navigator.share({ title: scheme.scheme_name, text: msg, url: scheme.source_url || "https://myscheme.gov.in" }).catch(console.error);
        else window.open(`https://api.whatsapp.com/send?text=${encodeURIComponent(msg)}`, '_blank');
    };

    return (
        <div className="flex-1 w-full max-w-4xl mx-auto py-6 sm:py-8 px-4 sm:px-0">
            
            <div className="flex justify-between items-center mb-6">
                <button onClick={() => navigate(-1)} className="print:hidden flex items-center gap-2 text-xs font-bold text-slate-500 dark:text-zinc-400 hover:text-slate-900 dark:hover:text-white uppercase tracking-widest transition-colors">
                    <ArrowLeft size={16} /> Back
                </button>
                <div className="flex gap-2">
                    <button onClick={handleShare} className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-zinc-900 border border-slate-200 dark:border-white/5 text-slate-600 dark:text-zinc-300 hover:bg-slate-50 dark:hover:bg-zinc-800 rounded-xl font-bold text-sm transition-all shadow-sm print:hidden">
                        <Share2 size={15} /> <span className="hidden sm:inline">Share</span>
                    </button>
                    <button onClick={() => window.open(`/scheme/${id}/print`, '_blank')} className="print:hidden flex items-center gap-2 px-4 py-2 bg-white dark:bg-zinc-900 border border-slate-200 dark:border-white/5 text-slate-600 dark:text-zinc-300 hover:bg-slate-50 dark:hover:bg-zinc-800 rounded-xl font-bold text-sm transition-all shadow-sm">
                        <Printer size={15} /> <span className="hidden sm:inline">Print / Download</span>
                    </button>
                    <button onClick={handleSave} disabled={isSaving} className={`flex items-center gap-2 px-4 py-2 border rounded-xl font-bold text-sm transition-all shadow-sm focus:outline-none print:hidden ${savedMsg === 'Saved' ? 'bg-emerald-50 dark:bg-emerald-500/10 border-emerald-200 dark:border-emerald-500/20 text-emerald-600 dark:text-emerald-400' : 'bg-white dark:bg-zinc-900 border-slate-200 dark:border-white/5 text-orange-600 dark:text-orange-400 hover:bg-slate-50 dark:hover:bg-zinc-800'}`}>
                        {savedMsg === "Saved" ? <BookmarkCheck size={15} /> : <BookmarkPlus size={15} />} 
                        <span className="hidden sm:inline">{savedMsg === 'Saved' ? 'Saved' : 'Save'}</span>
                    </button>
                </div>
            </div>

            <div className="pro-card rounded-[2rem] p-6 sm:p-10 mb-6 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-orange-500/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3" />
                <div className="flex justify-between items-start gap-6 relative z-10">
                    <div>
                        <div className="flex flex-wrap items-center gap-2 mb-4">
                            {statusBadge}
                            {scheme.scheme_category && <span className="bg-slate-50 dark:bg-zinc-800 text-slate-500 dark:text-zinc-400 border border-slate-200 dark:border-white/5 px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-widest uppercase">{scheme.scheme_category}</span>}
                            {scheme.scheme_type === "Central" && <span className="bg-slate-50 dark:bg-zinc-800 text-slate-500 dark:text-zinc-400 border border-slate-200 dark:border-white/5 px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-widest uppercase">Central</span>}
                        </div>
                        <h1 className="text-3xl sm:text-5xl font-black text-slate-900 dark:text-white tracking-tighter mb-4 leading-tight">
                            {scheme.scheme_name}
                        </h1>
                        <p className="text-slate-500 dark:text-zinc-400 font-medium text-sm flex items-center gap-2">
                            <span className="w-1.5 h-1.5 rounded-full bg-slate-300 dark:bg-zinc-600"></span>
                            {scheme.ministry || "Ministry of Social Justice and Empowerment"}
                        </p>
                    </div>
                    <div className="shrink-0 hidden md:flex items-center justify-center">
                        <div className="w-16 h-16 rounded-2xl bg-white dark:bg-zinc-900 border border-slate-200 dark:border-white/5 flex items-center justify-center shadow-sm -rotate-3 hover:rotate-0 transition-transform duration-500">
                            {iconBadge}
                        </div>
                    </div>
                </div>
            </div>

            {/* ── AI Verdict Hero Card ─────────────────────────────────────── */}
            {/* Always visible and prominent — this IS the primary reason for opening the scheme */}
            {resultData?.improvement_suggestion && (
                <div className="relative rounded-[2rem] overflow-hidden mb-6 border border-orange-500/20 shadow-lg shadow-orange-500/5">
                    {/* Top gradient strip */}
                    <div className="h-1 w-full bg-gradient-to-r from-orange-600 via-rose-500 to-amber-400" />
                    <div className="bg-[#0D0D1A] p-6 sm:p-8">
                        <div className="flex items-center gap-3 mb-4">
                            <div className="w-9 h-9 rounded-xl bg-orange-500/20 flex items-center justify-center shrink-0">
                                <Sparkles size={18} className="text-orange-400" />
                            </div>
                            <div>
                                <p className="text-orange-400 text-[10px] font-black tracking-[0.2em] uppercase">SchemeWise AI Analysis</p>
                                <p className="text-zinc-400 text-[10px]">Personalized reasoning based on your profile</p>
                            </div>
                        </div>
                        <p className="text-white leading-relaxed text-sm font-medium">
                            {resultData.improvement_suggestion}
                        </p>
                        {/* Ask AI context button */}
                        <div className="print:hidden flex flex-wrap gap-3 mt-5">
                            <button
                                onClick={() => navigate('/bot', { state: { initialQuery: `Please provide a detailed explanation of the scheme "${scheme.scheme_name}" and explain exactly how and why I am eligible for it based on my profile.` } })}
                                className="flex items-center gap-2 px-4 py-2 bg-orange-500/10 hover:bg-orange-500/20 border border-orange-500/30 text-orange-400 rounded-xl text-xs font-bold transition-all"
                            >
                                <Bot size={14} /> Ask AI about this scheme
                            </button>
                            <button
                                onClick={() => toggleSpeech(resultData.improvement_suggestion)}
                                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${isSpeaking ? 'bg-orange-500 border border-orange-400 text-white shadow-lg shadow-orange-500/20' : 'bg-white/5 hover:bg-white/10 border border-white/10 text-zinc-300'}`}
                            >
                                {isSpeaking ? <span className="flex items-center gap-1"><div className="w-1.5 h-1.5 bg-white rounded-full animate-ping"></div> Stop Audio</span> : <span className="flex items-center gap-1"><Play size={14} /> Listen</span>}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── AI Fetch section (detailed explanation) ──────────────────── */}
            {!aiExplanation ? (
                <button 
                    onClick={() => fetchAiExplanation()} 
                    disabled={isAiLoading}
                    className="w-full flex items-center justify-center gap-2 py-4 mb-6 rounded-2xl bg-orange-50 dark:bg-orange-500/10 border border-orange-200 dark:border-orange-500/20 text-orange-600 dark:text-orange-400 font-bold hover:bg-orange-100 dark:hover:bg-orange-500/20 transition-all shadow-sm focus:outline-none focus:ring-2 focus:ring-orange-500/30 disabled:opacity-50"
                >
                    {isAiLoading ? (
                        <div className="flex items-center gap-2 text-sm tracking-wide">
                            <div className="w-4 h-4 border-2 border-orange-200 border-t-orange-600 rounded-full animate-spin"></div>
                            INITIALIZING AI ENGINE...
                        </div>
                    ) : (
                        <div className="flex items-center gap-2 text-sm tracking-widest uppercase">
                            <Sparkles size={16} /> Get Application Guide
                        </div>
                    )}
                </button>
            ) : (
                <div className="mb-6 flex flex-col gap-3">
                    <div className="flex justify-end">
                        <button 
                            onClick={() => toggleSpeech(aiExplanation)}
                            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-bold transition-colors ${isSpeaking ? 'bg-orange-500/10 text-orange-600 dark:text-orange-400' : 'hover:bg-slate-100 dark:hover:bg-zinc-800 text-slate-500 dark:text-zinc-400' }`}
                        >
                            {isSpeaking ? <span className="flex items-center gap-1"><div className="w-1.5 h-1.5 bg-orange-500 rounded-full animate-ping"></div>Stop Audio</span> : <span className="flex items-center gap-1"><Play size={12} /> Play Audio</span>}
                        </button>
                    </div>
                
                    <div className="pro-card rounded-[2rem] p-6 sm:p-8 relative overflow-hidden">
                        <h3 className="text-orange-600 dark:text-orange-400 font-bold text-[10px] tracking-[0.2em] mb-4 flex items-center gap-2 uppercase">
                            <Sparkles size={14} /> Application Guide
                        </h3>
                        <div className="text-slate-700 dark:text-zinc-300 text-sm leading-relaxed font-medium whitespace-pre-wrap">
                            {aiExplanation}
                        </div>
                    </div>
                </div>
            )}

            <div className="pro-card rounded-[2rem] overflow-hidden mb-8">
                <div className="flex border-b border-slate-100 dark:border-white/5 px-2 pt-2">
                    <button onClick={() => setActiveTab('overview')} className={`px-6 py-3 text-sm font-bold flex items-center justify-center gap-2 border-b-2 transition-colors ${activeTab === 'overview' ? 'border-orange-500 text-slate-900 dark:text-white' : 'border-transparent text-slate-500 dark:text-zinc-500 hover:text-slate-700 dark:text-zinc-300'}`}>
                         Overview
                    </button>
                    <button onClick={() => setActiveTab('eligibility')} className={`px-6 py-3 text-sm font-bold flex items-center justify-center gap-2 border-b-2 transition-colors ${activeTab === 'eligibility' ? 'border-orange-500 text-slate-900 dark:text-white' : 'border-transparent text-slate-500 dark:text-zinc-500 hover:text-slate-700 dark:text-zinc-300'}`}>
                         Parameters
                    </button>
                </div>

                <div className="p-6 sm:p-8">
                    {activeTab === 'overview' && (
                        <div className="text-slate-600 dark:text-zinc-400 text-sm leading-relaxed font-medium space-y-4">
                            <p>Official government program targeted at improving socio-economic outcomes. {scheme.state_applicable !== 'ALL' ? `Targeted at residents of ${scheme.state_applicable}.` : 'Universal central government scheme applicable across India.'}</p>
                            <p>This scheme provides direct benefit transfers (DBT) and/or non-monetary assistance directly to the beneficiaries. Registration and validation occur entirely online through direct portals.</p>
                        </div>
                    )}

                    {activeTab === 'eligibility' && (
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                            {[
                                ['Age Profile', scheme.target_age_min ? `${scheme.target_age_min}+` : '0+ to ' + (scheme.target_age_max ? scheme.target_age_max : 'No Limit')],
                                ['Identity', scheme.target_gender || 'All'],
                                ['Financials', scheme.income_max ? `Up to ₹${scheme.income_max.toLocaleString('en-IN')}` : 'No Limit'],
                                ['Social Matrix', scheme.eligible_categories?.includes('ALL') ? 'Universal' : (Array.isArray(scheme.eligible_categories) ? scheme.eligible_categories.join(', ') : scheme.eligible_categories) || 'All'],
                                ['Career Core', scheme.occupation_required?.length > 0 ? (Array.isArray(scheme.occupation_required) ? scheme.occupation_required.join(', ') : scheme.occupation_required) : 'Any'],
                                ['Geofence', scheme.rural_urban || 'Both'],
                            ].map(([label, val], i) => (
                                <div key={i} className="bg-slate-50 dark:bg-zinc-900 border border-slate-100 dark:border-white/5 rounded-xl p-4 flex flex-col gap-1">
                                    <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400 dark:text-zinc-500">{label}</span>
                                    <span className="text-slate-900 dark:text-white font-bold text-sm line-clamp-1">{val}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="flex justify-end mt-12 border-t border-slate-200 dark:border-white/5 pt-8">
                {scheme.source_url ? (
                    <a href={scheme.source_url} target="_blank" rel="noreferrer" className="inline-flex items-center justify-center gap-2 px-8 py-4 bg-orange-600 hover:bg-orange-700 text-slate-900 dark:text-white rounded-xl font-bold transition-all shadow-[0_4px_14px_0_rgba(234,88,12,0.39)] hover:shadow-[0_6px_20px_rgba(234,88,12,0.23)] hover:-translate-y-0.5 focus:outline-none focus:ring-4 focus:ring-orange-500/50">
                        Initiate Application <ExternalLink size={16} />
                    </a>
                ) : (
                    <button disabled className="inline-flex items-center justify-center gap-2 px-8 py-4 bg-slate-100 dark:bg-zinc-800 text-slate-400 dark:text-zinc-600 rounded-xl font-bold cursor-not-allowed border border-slate-200 dark:border-white/5">
                        Portal Offline
                    </button>
                )}
            </div>

        </div>
    );
};

export default SchemeDetailPage;
