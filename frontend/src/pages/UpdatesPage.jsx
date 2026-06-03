import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSchemes } from '../hooks/useSchemes';
import { useProfile } from '../context/ProfileContext';
import { Bell, Sparkles, TrendingUp, AlertCircle, ChevronRight, FileText, Info } from 'lucide-react';

const UpdatesPage = () => {
    const navigate = useNavigate();
    const [filter, setFilter] = useState('ALL');
    const [updates, setUpdates] = useState([]);
    const [alerts, setAlerts] = useState([]);
    const { fetchUpdates, evaluateProfile } = useSchemes();
    const { getActiveProfile } = useProfile();
    const [loading, setLoading] = useState(true);
    const [alertsLoading, setAlertsLoading] = useState(true);

    const profile = getActiveProfile();

    // Load directory log from backend
    useEffect(() => {
        const load = async () => {
            try { 
                const data = await fetchUpdates(); 
                if (data) setUpdates(data); 
            } catch (err) { console.error(err); } finally { setLoading(false); }
        };
        load();
    }, [fetchUpdates]);

    // Build REAL alerts from the user's actual eligible schemes
    useEffect(() => {
        const buildAlerts = async () => {
            if (!profile) { setAlertsLoading(false); return; }

            try {
                const results = await evaluateProfile(profile);
                if (!results || results.length === 0) { setAlertsLoading(false); return; }

                const generatedAlerts = [];
                const eligible = results.filter(s => s.status === 'Eligible');
                const partial = results.filter(s => s.status === 'Partially Eligible');

                // Real rule-based alerts — only triggered if appropriate for THIS user
                // 1. BPL housing — only for BPL/low income users
                if (profile.isBPL === 'Yes' || (profile.income && parseInt(profile.income) < 100000)) {
                    const housingScheme = eligible.find(s => 
                        s.scheme_name?.toLowerCase().includes('housing') || 
                        s.scheme_name?.toLowerCase().includes('awas') ||
                        s.scheme_name?.toLowerCase().includes('pmay')
                    );
                    if (housingScheme) {
                        generatedAlerts.push({
                            title: 'Housing Subsidy Match Found',
                            desc: `Based on your income profile, you match "${housingScheme.scheme_name}". Application windows are typically short — review now.`,
                            scheme_id: housingScheme.scheme_id,
                            scheme: housingScheme,
                            icon: AlertCircle,
                            color: 'text-amber-600',
                            bg: 'bg-amber-50 border border-amber-200'
                        });
                    }
                }

                // 2. Agriculture/Farmer schemes — only for farmers
                const isFarmer = profile.occupation?.toLowerCase().includes('farm') ||
                                 profile.occupation?.toLowerCase().includes('agri') ||
                                 profile.occupation?.toLowerCase().includes('kisan');
                if (isFarmer) {
                    const farmScheme = eligible.find(s => 
                        s.scheme_name?.toLowerCase().includes('kisan') ||
                        s.scheme_name?.toLowerCase().includes('farm') ||
                        s.scheme_category?.toLowerCase().includes('agriculture')
                    );
                    if (farmScheme) {
                        generatedAlerts.push({
                            title: 'Agriculture Scheme: New Benefit Cycle',
                            desc: `You are eligible for "${farmScheme.scheme_name}". A new disbursement cycle has been announced — verify your bank linkage promptly.`,
                            scheme_id: farmScheme.scheme_id,
                            scheme: farmScheme,
                            icon: TrendingUp,
                            color: 'text-emerald-600',
                            bg: 'bg-emerald-50 border border-emerald-200'
                        });
                    }
                }

                // 3. SC/ST/OBC schemes — only for relevant category users
                const reservedCategories = ['SC', 'ST', 'OBC', 'Minority'];
                if (reservedCategories.includes(profile.category)) {
                    const socialScheme = eligible.find(s =>
                        s.scheme_name?.toLowerCase().includes(profile.category.toLowerCase()) ||
                        s.scheme_category?.toLowerCase().includes('social') ||
                        s.eligible_categories?.includes(profile.category)
                    );
                    if (socialScheme) {
                        generatedAlerts.push({
                            title: `${profile.category} Category Scheme Match`,
                            desc: `"${socialScheme.scheme_name}" has a new enrollment round open for ${profile.category} citizens in ${profile.state || 'your state'}.`,
                            scheme_id: socialScheme.scheme_id,
                            scheme: socialScheme,
                            icon: Sparkles,
                            color: 'text-orange-500',
                            bg: 'bg-white dark:bg-zinc-900 border border-orange-200'
                        });
                    }
                }

                // 4. Female-only scholarship — ONLY for female students
                if (profile.gender === 'Female' && profile.isStudent === 'Yes') {
                    const femaleScheme = eligible.find(s =>
                        s.target_gender === 'Female' ||
                        s.scheme_name?.toLowerCase().includes('mahila') ||
                        s.scheme_name?.toLowerCase().includes('women') ||
                        s.scheme_name?.toLowerCase().includes('girl')
                    );
                    if (femaleScheme) {
                        generatedAlerts.push({
                            title: 'Women Scholarship Disbursement',
                            desc: `Direct DBT transfers for "${femaleScheme.scheme_name}" have been announced for this quarter. Ensure your bank details are updated.`,
                            scheme_id: femaleScheme.scheme_id,
                            scheme: femaleScheme,
                            icon: Sparkles,
                            color: 'text-pink-600',
                            bg: 'bg-pink-50 border border-pink-200'
                        });
                    }
                }

                // 5. Male student scholarships — only for male students
                if (profile.gender === 'Male' && profile.isStudent === 'Yes') {
                    const studentScheme = eligible.find(s =>
                        s.scheme_category?.toLowerCase().includes('education') ||
                        s.scheme_name?.toLowerCase().includes('scholar') ||
                        s.scheme_name?.toLowerCase().includes('merit')
                    );
                    if (studentScheme) {
                        generatedAlerts.push({
                            title: 'Scholarship Application Window Open',
                            desc: `You match "${studentScheme.scheme_name}". Scholarship applications are accepted during this period — apply before the deadline closes.`,
                            scheme_id: studentScheme.scheme_id,
                            scheme: studentScheme,
                            icon: Sparkles,
                            color: 'text-orange-500',
                            bg: 'bg-white dark:bg-zinc-900 border border-orange-500/30'
                        });
                    }
                }

                // 6. General high-value eligible scheme alert (always show top eligible scheme if no other alerts triggered)
                if (generatedAlerts.length === 0 && eligible.length > 0) {
                    const topScheme = eligible[0];
                    generatedAlerts.push({
                        title: `New Match: ${topScheme.scheme_name.length > 48 ? topScheme.scheme_name.slice(0, 48) + '…' : topScheme.scheme_name}`,
                        desc: `This scheme matches your profile in ${profile.state || 'your state'}. Your eligibility score is ${(topScheme.score_percentage || 0).toFixed(0)}%. Review and apply now.`,
                        scheme_id: topScheme.scheme_id,
                        scheme: topScheme,
                        icon: TrendingUp,
                        color: 'text-orange-500',
                        bg: 'bg-white dark:bg-zinc-900 border border-orange-500/30'
                    });
                }

                // 7. Improvement tip — if there are partial matches
                if (partial.length > 0) {
                    const improvable = partial[0];
                    generatedAlerts.push({
                        title: 'Boost Your Eligibility',
                        desc: `You are a partial match for "${improvable.scheme_name}". ${improvable.improvement_suggestion || 'Updating your profile details may unlock full eligibility.'}`,
                        scheme_id: improvable.scheme_id,
                        scheme: improvable,
                        icon: Info,
                        color: 'text-slate-600 dark:text-zinc-300',
                        bg: 'bg-transparent border border-slate-200 dark:border-white/5'
                    });
                }

                setAlerts(generatedAlerts);
            } catch (err) {
                console.error('Alert generation error:', err);
            } finally {
                setAlertsLoading(false);
            }
        };

        buildAlerts();
    }, [profile, evaluateProfile]);

    const filters = [
        { key: 'ALL', label: 'All Updates' }, 
        { key: 'NEW SCHEME', label: 'New Launches' }, 
        { key: 'ENHANCED', label: 'Policy Changes' }, 
        { key: 'DEPRECIATED', label: 'Archived' }
    ];

    const filtered = filter === 'ALL' ? updates : updates.filter(u => u.type === filter);

    return (
        <div className="flex-1 w-full max-w-4xl mx-auto py-8 px-4 sm:px-6">
            <div className="mb-6">
                <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tighter mb-1 flex items-center gap-2">
                    <Bell className="text-orange-500" size={24} /> Centralised Feed
                </h1>
                <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium">Alerts and policy announcements tailored to your profile.</p>
            </div>

            {/* Personalised Alerts Feed */}
            <div className="mb-8">
                <h2 className="text-xs font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest flex items-center gap-2 mb-3">
                    <Sparkles size={14} className="text-amber-500" /> Personalised Alerts
                </h2>

                {!profile ? (
                    <div className="bg-transparent border border-slate-200 dark:border-white/5 rounded-2xl p-6 text-center text-slate-500 dark:text-zinc-400 text-sm font-medium">
                        Complete your profile to see personalised alerts.
                    </div>
                ) : alertsLoading ? (
                    <div className="flex justify-center py-10">
                        <div className="w-8 h-8 border-4 border-slate-200 dark:border-white/5 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : alerts.length === 0 ? (
                    <div className="bg-transparent border border-slate-200 dark:border-white/5 rounded-2xl p-6 text-center text-slate-500 dark:text-zinc-400 text-sm font-medium">
                        No personalised alerts at this time. Check back after updating your profile.
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {alerts.map((alert, idx) => (
                            <div key={idx} className={`rounded-xl p-5 flex gap-4 ${alert.bg} shadow-2xl shadow-black/5 dark:shadow-black/40 transition-transform hover:-translate-y-1`}>
                                <div className="w-10 h-10 rounded-lg pro-card shadow-2xl shadow-black/5 dark:shadow-black/40 flex items-center justify-center shrink-0">
                                    <alert.icon size={20} className={alert.color} />
                                </div>
                                <div className="flex-1 min-w-0">
                                    <h3 className="text-slate-900 dark:text-white font-bold text-[15px] mb-1">{alert.title}</h3>
                                    <p className="text-slate-500 dark:text-zinc-400 text-sm leading-relaxed mb-3">{alert.desc}</p>
                                    {alert.scheme_id && (
                                        <button
                                            onClick={() => navigate(`/scheme/${alert.scheme_id}`, { state: { resultData: alert.scheme } })}
                                            className={`text-xs font-bold uppercase tracking-widest ${alert.color} flex items-center gap-1 hover:underline`}
                                        >
                                            View Scheme <ChevronRight size={14} />
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* General Directory Log */}
            <h2 className="text-xs font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest flex items-center gap-2 mb-3">
                <FileText size={14} className="text-orange-500" /> Directory Log
            </h2>
            <div className="flex gap-2 flex-wrap mb-6">
                {filters.map(f => (
                    <button key={f.key} onClick={() => setFilter(f.key)}
                        className={`px-5 py-2.5 rounded-full text-sm font-bold transition-all ${filter === f.key ? 'bg-orange-600 text-slate-900 dark:text-white shadow-2xl shadow-black/5 dark:shadow-black/40' : 'pro-card text-slate-600 dark:text-zinc-300 hover:border-slate-200 dark:border-zinc-800/30'}`}>
                        {f.label}
                    </button>
                ))}
            </div>

            <div className="space-y-4">
                {loading ? (
                    <div className="flex justify-center py-20">
                        <div className="w-10 h-10 border-4 border-slate-200 dark:border-white/5 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 text-center py-16">
                        <span className="text-5xl block mb-4">📭</span>
                        <p className="text-slate-500 dark:text-zinc-400 font-medium">No updates matching this filter.</p>
                    </div>
                ) : (
                    filtered.map(update => {
                        let theme = { border: 'border-l-zinc-400', bg: 'bg-transparent', text: 'text-slate-500 dark:text-zinc-400', bar: 'border-slate-200 dark:border-white/5' };
                        if (update.type === 'NEW SCHEME') theme = { border: 'border-l-[#142F58]', bg: 'bg-white dark:bg-zinc-900', text: 'text-orange-500', bar: 'border-orange-200' };
                        else if (update.type === 'ENHANCED') theme = { border: 'border-l-emerald-500', bg: 'bg-emerald-50', text: 'text-emerald-600', bar: 'border-emerald-200' };
                        else if (update.type === 'DEPRECIATED') theme = { border: 'border-l-rose-400', bg: 'bg-rose-50', text: 'text-rose-600', bar: 'border-rose-200' };

                        return (
                            <div key={update.id} className={`pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 p-6 border-l-4 ${theme.border}`}>
                                <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-3 mb-3">
                                    <div className="flex items-center gap-2 flex-wrap">
                                        <span className={`text-[10px] font-bold px-2.5 py-1 uppercase tracking-wider rounded-lg border ${theme.bg} ${theme.text} ${theme.bar}`}>{update.type}</span>
                                        <span className="text-xs text-slate-500 dark:text-zinc-400 font-medium">{update.date}</span>
                                    </div>
                                    {update.scheme_id && (
                                        <button
                                            onClick={() => navigate(`/scheme/${update.scheme_id}`)}
                                            className="text-xs font-bold text-orange-500 border border-orange-200 rounded-xl px-3 py-1.5 hover:bg-white dark:bg-zinc-900 transition-all hidden sm:block"
                                        >
                                            View Policy
                                        </button>
                                    )}
                                </div>
                                <h3 className="text-slate-900 dark:text-white font-bold text-base mb-2">{update.schemeName}</h3>
                                <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium leading-relaxed bg-transparent p-3 rounded-xl">{update.description}</p>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
};

export default UpdatesPage;
