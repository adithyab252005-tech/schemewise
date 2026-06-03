import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, Loader2 } from 'lucide-react';
import axios from 'axios';

const API = 'http://180.235.121.245:8060/api/v1/users';

const ForgotPasswordPage = () => {
    const [email, setEmail] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await axios.post(`${API}/forgot-password`, { email });
            setSubmitted(true);
        } catch (err) {
            const msg = err.response?.data?.error || 'Something went wrong. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center px-5 font-sans" style={{ background: 'linear-gradient(180deg, #e8eaf0 0%, #f0f2f8 100%)' }}>
            <div className="w-full max-w-sm">
                <div className="flex flex-col items-center mb-8">
                    <div className="w-14 h-14 rounded-[18px] bg-orange-600 flex items-center justify-center text-slate-900 dark:text-white font-bold text-2xl shadow-md mb-5">S</div>
                    <h1 className="text-[28px] font-extrabold text-slate-900 dark:text-white tracking-tighter tracking-tight mb-1">Forgot Password?</h1>
                    <p className="text-[14px] text-slate-500 dark:text-zinc-400 font-medium text-center">Enter your email and we'll send you a reset link.</p>
                </div>

                <div className="pro-card rounded-3xl shadow-2xl shadow-black/5 dark:shadow-black/40 border border-slate-200 dark:border-white/5 p-7">
                    {submitted ? (
                        <div className="flex flex-col items-center gap-4 text-center py-4">
                            <div className="text-5xl">📬</div>
                            <h2 className="text-slate-900 dark:text-white font-extrabold text-xl">Check your inbox</h2>
                            <p className="text-slate-500 dark:text-zinc-400 text-sm">
                                If an account exists for <span className="text-orange-500 font-bold">{email}</span>, you'll receive a password reset email shortly.
                            </p>
                            <Link to="/login" className="flex items-center gap-1 text-orange-500 hover:underline text-sm font-bold mt-2">
                                <ArrowLeft size={14} /> Back to Sign In
                            </Link>
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                            <div className="flex flex-col gap-1.5">
                                <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">Email Address</label>
                                <input
                                    type="email"
                                    placeholder="you@example.com"
                                    value={email}
                                    onChange={e => { setEmail(e.target.value); setError(''); }}
                                    required
                                    className="w-full px-4 py-3.5 pro-card rounded-2xl text-sm text-slate-700 dark:text-zinc-200 focus:ring-2 focus:ring-orange-500/30/20 focus:border-slate-200 dark:border-white/5 outline-none transition-all placeholder:text-slate-500 dark:text-zinc-400 shadow-2xl shadow-black/5 dark:shadow-black/40"
                                />
                            </div>

                            {error && (
                                <div className="bg-red-50 border border-red-100 rounded-xl p-3 text-sm text-red-600 font-medium">
                                    {error}
                                </div>
                            )}

                            <button
                                type="submit"
                                disabled={!email || loading}
                                className={`w-full py-3.5 rounded-2xl text-[16px] font-bold transition-all flex items-center justify-center gap-2 ${email && !loading ? 'bg-orange-600 text-slate-900 dark:text-white hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30' : 'bg-zinc-300 text-slate-900 dark:text-white cursor-not-allowed'}`}
                            >
                                {loading ? <><Loader2 size={18} className="animate-spin" /> Sending...</> : 'Send Reset Link'}
                            </button>

                            <Link to="/login" className="flex items-center justify-center gap-1 text-slate-500 dark:text-zinc-400 hover:text-slate-700 dark:text-zinc-200 text-sm font-medium transition-colors">
                                <ArrowLeft size={14} /> Back to Sign In
                            </Link>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ForgotPasswordPage;
