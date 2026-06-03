import { Link } from 'react-router-dom';
import { Cpu, Sparkles, RefreshCw, ArrowRight } from 'lucide-react';

const features = [
    { icon: <Cpu size={20} />, title: 'Deterministic Matching', desc: 'Your profile was run through our rules engine against hundreds of live schemes.', bg: 'bg-orange-100', color: 'text-orange-500' },
    { icon: <Sparkles size={20} />, title: 'AI-Powered Dashboard', desc: 'Your personalized dashboard shows exact eligibility results with AI explanations.', bg: 'bg-orange-100', color: 'text-orange-500' },
    { icon: <RefreshCw size={20} />, title: 'Live Monitoring', desc: 'Background agents alert you as soon as scheme rules change or new schemes launch.', bg: 'bg-emerald-100', color: 'text-emerald-600' },
];

const JourneyIntroPage = () => {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center px-5 py-16 bg-transparent">
            <div className="max-w-md mx-auto text-center w-full">
                <div className="text-5xl mb-5">🚀</div>
                <h1 className="text-[30px] font-extrabold text-slate-900 dark:text-white tracking-tighter tracking-tight mb-3">Your Journey Begins</h1>
                <p className="text-slate-500 dark:text-zinc-400 text-[15px] mb-10 leading-relaxed">
                    Here's what's now working for you inside your personalized SchemeWise dashboard.
                </p>

                <div className="flex flex-col gap-4 mb-10">
                    {features.map((f, i) => (
                        <div key={i} className="pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 p-5 flex items-start gap-4 text-left">
                            <div className={`w-11 h-11 rounded-2xl ${f.bg} ${f.color} flex items-center justify-center flex-shrink-0`}>{f.icon}</div>
                            <div>
                                <h3 className="font-bold text-slate-900 dark:text-white tracking-tight mb-1">{f.title}</h3>
                                <p className="text-slate-500 dark:text-zinc-400 text-sm leading-relaxed">{f.desc}</p>
                            </div>
                        </div>
                    ))}
                </div>

                <Link to="/dashboard">
                    <button className="w-full h-14 bg-orange-600 text-slate-900 dark:text-white font-bold text-[16px] rounded-2xl shadow-md hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all flex items-center justify-center gap-2">
                        Enter Dashboard <ArrowRight size={20} />
                    </button>
                </Link>
            </div>
        </div>
    );
};

export default JourneyIntroPage;
