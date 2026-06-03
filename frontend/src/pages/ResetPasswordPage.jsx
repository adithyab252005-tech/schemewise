import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';
import axios from 'axios';
import { Loader2, CheckCircle, XCircle } from 'lucide-react';

const API = 'http://180.235.121.245:8060/api/v1/users';

const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { login } = useProfile();

    const token = searchParams.get('token');
    const action = searchParams.get('action'); // 'login' for magic login

    const [status, setStatus] = useState('loading'); // 'loading' | 'enter_password' | 'success' | 'error'
    const [message, setMessage] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (!token) {
            setStatus('error');
            setMessage('Invalid or missing reset token.');
            return;
        }

        // Magic login: auto-validate token and log in immediately
        if (action === 'login') {
            axios.post(`${API}/reset-password`, { token })
                .then(res => {
                    const user = res.data.user;
                    if (user) {
                        login(user.is_admin ? 'admin' : 'user', user.email, user);
                        setStatus('success');
                        setMessage(`Welcome back, ${user.name || 'Citizen'}! Logging you in...`);
                        setTimeout(() => navigate('/dashboard', { replace: true }), 1500);
                    } else {
                        setStatus('error');
                        setMessage('Token is valid but no user data returned.');
                    }
                })
                .catch(err => {
                    setStatus('error');
                    setMessage(err.response?.data?.error || 'This magic link is invalid or has expired.');
                });
        } else {
            // Regular reset flow: just show the password form
            setStatus('enter_password');
        }
    }, [token, action]);

    const handleSetPassword = async (e) => {
        e.preventDefault();
        if (newPassword !== confirm) { setMessage('Passwords do not match.'); return; }
        if (newPassword.length < 6) { setMessage('Password must be at least 6 characters.'); return; }
        setSubmitting(true);
        setMessage('');
        try {
            await axios.post(`${API}/reset-password`, { token, newPassword });
            setStatus('success');
            setMessage('Password updated! Redirecting to login...');
            setTimeout(() => navigate('/login', { replace: true }), 2000);
        } catch (err) {
            setMessage(err.response?.data?.error || 'Failed to reset password. The link may have expired.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center px-5 font-sans" style={{ background: 'linear-gradient(180deg, #e8eaf0 0%, #f0f2f8 100%)' }}>
            <div className="w-full max-w-sm">
                <div className="flex flex-col items-center mb-8">
                    <div className="w-14 h-14 rounded-[18px] bg-orange-600 flex items-center justify-center text-slate-900 dark:text-white font-bold text-2xl shadow-md mb-5">S</div>
                    <h1 className="text-[26px] font-extrabold text-slate-900 dark:text-white tracking-tighter tracking-tight mb-1">
                        {action === 'login' ? 'Magic Login' : 'Reset Password'}
                    </h1>
                </div>

                <div className="pro-card rounded-3xl shadow-2xl shadow-black/5 dark:shadow-black/40 border border-slate-200 dark:border-white/5 p-7 flex flex-col items-center gap-5 text-center">
                    {status === 'loading' && (
                        <>
                            <Loader2 size={36} className="animate-spin text-orange-500" />
                            <p className="text-slate-500 dark:text-zinc-400 font-medium text-sm">Verifying your link...</p>
                        </>
                    )}

                    {status === 'success' && (
                        <>
                            <CheckCircle size={40} className="text-emerald-500" />
                            <p className="text-slate-900 dark:text-white font-bold text-lg">{message}</p>
                        </>
                    )}

                    {status === 'error' && (
                        <>
                            <XCircle size={40} className="text-red-500" />
                            <p className="text-red-600 font-semibold text-sm">{message}</p>
                            <a href="/forgot-password" className="text-orange-500 text-sm font-bold hover:underline">Request a new link →</a>
                        </>
                    )}

                    {status === 'enter_password' && (
                        <form onSubmit={handleSetPassword} className="w-full flex flex-col gap-4 text-left">
                            <p className="text-slate-500 dark:text-zinc-400 text-sm text-center">Enter a new password for your account.</p>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">New Password</label>
                                <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} required minLength={6}
                                    className="w-full px-4 py-3.5 pro-card rounded-2xl text-sm text-slate-700 dark:text-zinc-200 focus:ring-2 focus:ring-orange-500/30/20 focus:border-slate-200 dark:border-white/5 outline-none transition-all" />
                            </div>
                            <div className="flex flex-col gap-1.5">
                                <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">Confirm Password</label>
                                <input type="password" value={confirm} onChange={e => setConfirm(e.target.value)} required
                                    className="w-full px-4 py-3.5 pro-card rounded-2xl text-sm text-slate-700 dark:text-zinc-200 focus:ring-2 focus:ring-orange-500/30/20 focus:border-slate-200 dark:border-white/5 outline-none transition-all" />
                            </div>
                            {message && <p className="text-red-500 text-xs font-medium text-center">{message}</p>}
                            <button type="submit" disabled={submitting}
                                className="w-full py-3.5 rounded-2xl text-[15px] font-bold bg-orange-600 text-slate-900 dark:text-white hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all flex items-center justify-center gap-2">
                                {submitting ? <><Loader2 size={16} className="animate-spin" /> Saving...</> : 'Set New Password'}
                            </button>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ResetPasswordPage;
