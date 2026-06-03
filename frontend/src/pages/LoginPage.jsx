import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useProfile } from '../context/ProfileContext';
import { Eye, EyeOff, ShieldCheck, ArrowRight } from 'lucide-react';

const LoginPage = () => {
    const navigate = useNavigate();
    const { login } = useProfile();
    const [credentials, setCredentials] = useState({ email: '', password: '' });
    const [loading, setLoading] = useState(false);
    const [showPw, setShowPw] = useState(false);

    const handleChange = (e) => setCredentials(prev => ({ ...prev, [e.target.name]: e.target.value }));

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const res = await axios.post('http://180.235.121.245:8060/api/v1/users/login', {
                email: credentials.email,
                password: credentials.password
            });
            if (res.data?.user) {
                if (res.data.user.is_admin) {
                    login('admin', res.data.user.email, res.data.user, credentials.password);
                    navigate('/admin');
                } else {
                    login('user', res.data.user.email, res.data.user, credentials.password);
                    navigate('/dashboard');
                }
            }
        } catch (error) {
            alert(error.response?.data?.message || 'Invalid login credentials');
        } finally {
            setLoading(false);
        }
    };

    const isReady = credentials.email && credentials.password;

    return (
        <div className="min-h-screen flex">
            {/* Left — animated hero panel */}
            <div className="hidden lg:flex w-1/2 relative overflow-hidden flex-col items-center justify-center"
                style={{ background: 'linear-gradient(145deg, #09090b 0%, #171717 50%, #431407 100%)' }}>
                {/* Floating orbs */}
                <div className="absolute w-96 h-96 rounded-full opacity-10 blur-3xl floating"
                    style={{ background: 'radial-gradient(circle, #ea580c, transparent)', top: '10%', left: '10%', animationDelay: '0s' }} />
                <div className="absolute w-64 h-64 rounded-full opacity-10 blur-3xl floating"
                    style={{ background: 'radial-gradient(circle, #c2410c, transparent)', bottom: '15%', right: '15%', animationDelay: '2s' }} />

                <div className="relative z-10 text-center px-12">
                    <div className="w-20 h-20 rounded-3xl pro-card backdrop-blur-sm border border-transparent flex items-center justify-center mx-auto mb-8 shadow-2xl">
                        <ShieldCheck size={40} className="text-orange-400" strokeWidth={1.5} />
                    </div>
                    <h2 className="text-4xl font-black text-white tracking-tighter mb-4 leading-tight">
                        Your Gateway to<br />
                        <span className="text-orange-400">Government Welfare</span>
                    </h2>
                    <p className="text-white/80 text-base font-medium leading-relaxed max-w-sm mx-auto">
                        Discover thousands of government schemes you're eligible for. Personalised, instant, and free.
                    </p>

                    <div className="mt-10 grid grid-cols-3 gap-4">
                        {[['4,200+', 'Schemes'], ['100%', 'Free'], ['Instant', 'Results']].map(([val, label]) => (
                            <div key={label} className="pro-card/8 backdrop-blur-sm rounded-2xl p-4 border border-transparent">
                                <p className="text-xl font-black text-white tracking-tighter">{val}</p>
                                <p className="text-xs text-white/60 font-medium mt-0.5">{label}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Right — clean form */}
            <div className="flex-1 flex flex-col items-center justify-center px-6 py-12 bg-transparent">
                <div className="w-full max-w-sm animate-fade-in">
                    {/* Mobile logo */}
                    <div className="flex flex-col items-center mb-8 lg:hidden">
                        <div className="w-12 h-12 rounded-2xl bg-orange-600 flex items-center justify-center text-slate-900 dark:text-white font-black text-xl shadow-lg mb-4">S</div>
                        <h1 className="text-2xl font-black text-slate-900 dark:text-white tracking-tighter">SchemeWise</h1>
                    </div>

                    <div className="mb-8">
                        <h1 className="text-3xl font-black text-slate-900 dark:text-white tracking-tighter tracking-tight mb-1">Welcome back</h1>
                        <p className="text-slate-500 dark:text-zinc-400 font-medium text-sm">Sign in to your account to continue</p>
                    </div>

                    <div className="pro-card rounded-3xl shadow-[0_20px_60px_-15px_rgba(234,88,12,0.15)] dark:shadow-[0_20px_60px_-15px_rgba(234,88,12,0.08)] border border-white/5 p-7">
                        <form onSubmit={handleLogin} className="flex flex-col gap-5">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-xs font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Email</label>
                                <input type="email" name="email"
                                    value={credentials.email} onChange={handleChange} required
                                    className="w-full px-4 py-3.5 bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-2xl text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all placeholder:text-slate-500 dark:placeholder:text-zinc-500 font-medium" />
                            </div>

                            <div className="flex flex-col gap-1.5">
                                <label className="text-xs font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest">Password</label>
                                <div className="relative">
                                    <input type={showPw ? 'text' : 'password'} name="password"
                                        value={credentials.password} onChange={handleChange} required
                                        className="w-full px-4 py-3.5 pr-12 bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-2xl text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 outline-none transition-all placeholder:text-slate-500 dark:placeholder:text-zinc-500 font-medium" />
                                    <button type="button" onClick={() => setShowPw(v => !v)}
                                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 dark:text-zinc-400 hover:text-[#8A96A8] transition-colors">
                                        {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                </div>
                            </div>

                            <div className="flex justify-end -mt-2">
                                <Link to="/forgot-password" className="text-xs font-bold text-orange-500 hover:underline">Forgot Password?</Link>
                            </div>

                            <button type="submit" disabled={loading || !isReady}
                                className={`btn-shine w-full py-3.5 rounded-2xl text-[15px] font-bold transition-all flex items-center justify-center gap-2 shadow-md ${
                                    isReady
                                        ? 'bg-gradient-to-r from-orange-700 to-orange-600 text-slate-900 dark:text-white hover:shadow-lg hover:shadow-orange-500/30 hover:-translate-y-0.5'
                                        : 'bg-zinc-200 text-slate-500 dark:text-zinc-400 cursor-not-allowed'
                                }`}>
                                {loading ? (
                                    <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Signing in...</>
                                ) : (
                                    <>Sign In <ArrowRight size={16} /></>
                                )}
                            </button>
                        </form>

                        <div className="flex items-center justify-center gap-1.5 mt-6 text-sm">
                            <span className="text-slate-500 dark:text-zinc-400">No account?</span>
                            <Link to="/register" className="text-orange-500 font-bold hover:underline">Create one free →</Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
