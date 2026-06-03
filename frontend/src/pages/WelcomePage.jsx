import { Link } from 'react-router-dom';
import { ArrowRight, CheckCircle } from 'lucide-react';

const WelcomePage = () => {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center px-5 font-sans bg-transparent">
            <div className="text-center max-w-sm mx-auto">
                <div className="w-20 h-20 rounded-full bg-emerald-100 mx-auto flex items-center justify-center mb-6">
                    <CheckCircle className="text-emerald-500" size={38} />
                </div>
                <h1 className="text-[32px] font-extrabold text-slate-900 dark:text-white tracking-tighter tracking-tight mb-3">Account Created! 🎉</h1>
                <p className="text-slate-500 dark:text-zinc-400 text-[15px] leading-relaxed mb-10">
                    Welcome to SchemeWise. Let's fill in a few details so we can match you to the right schemes.
                </p>
                <Link to="/onboarding/details">
                    <button className="w-full h-14 bg-orange-600 text-slate-900 dark:text-white font-bold text-[16px] rounded-2xl shadow-md hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all flex items-center justify-center gap-2">
                        Complete Your Profile <ArrowRight size={20} />
                    </button>
                </Link>
            </div>
        </div>
    );
};

export default WelcomePage;
