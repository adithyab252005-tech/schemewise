import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';
import { Eye, EyeOff } from 'lucide-react';

const RegisterPage = () => {
  const { login } = useProfile();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [emailSent, setEmailSent] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [otp, setOtp] = useState('');
  const [verifying, setVerifying] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
        const publicDomains = ['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com', 'aol.com'];
        const emailDomain = formData.email.split('@')[1]?.toLowerCase();
        
        if (!publicDomains.includes(emailDomain)) {
            setError('Please use a public email provider (e.g., Gmail, Yahoo).');
            setLoading(false);
            return;
        }

        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
        if (!passwordRegex.test(formData.password)) {
            setError('Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, a number, and a special character (e.g., #, @).');
            setLoading(false);
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match.');
            setLoading(false);
            return;
        }

        const response = await fetch('http://180.235.121.245:8060/api/v1/users/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: formData.name,
                email: formData.email,
                password: formData.password
            })
        });
        const data = await response.json();
        // Check for error key explicitly (backend returns error on SMTP failure with 500)
        if (!response.ok || data.error) throw new Error(data.error || 'Registration failed');

        setEmailSent(true);
    } catch (err) {
        setError(err.message);
    } finally {
        setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    if (otp.length !== 6) {
        setError("Please enter the 6-digit verification code.");
        return;
    }
    setVerifying(true);
    setError(null);

    try {
        const response = await fetch('http://180.235.121.245:8060/api/v1/users/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: formData.email,
                otp: otp
            })
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Verification failed');

        // Log the user in with the verified token
        await login('user', data.user.email, data.user, null);
        navigate('/welcome');

    } catch (err) {
        setError(err.message);
    } finally {
        setVerifying(false);
    }
  };

  const inputClass = "w-full bg-slate-100 dark:bg-white/5 border border-slate-200 dark:border-white/10 rounded-xl px-4 py-3.5 text-slate-900 dark:text-white placeholder:text-slate-500 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition-all font-medium text-sm";
  const labelClass = "block text-xs font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-widest mb-1.5 pl-1";

  return (
    <div className="bg-transparent min-h-screen py-16 flex flex-col justify-center sm:py-24">
      <div className="relative py-3 sm:max-w-md sm:mx-auto w-full px-4">
        
        <div className="absolute inset-0 bg-gradient-to-r from-[#822c08] to-orange-500 shadow-xl transform -skew-y-6 sm:skew-y-0 sm:-rotate-6 sm:rounded-3xl"></div>
        
        <div className="relative pro-card shadow-xl sm:rounded-3xl px-6 py-10 sm:p-12 w-full">
            <div className="max-w-md mx-auto">
                
                {/* Header */}
                <div className="text-center mb-8">
                    <div className="w-16 h-16 bg-[#ea580c] rounded-2xl mx-auto flex items-center justify-center mb-4 shadow-md">
                        <span className="text-slate-900 dark:text-white font-black text-3xl leading-none">S</span>
                    </div>
                    <h1 className="text-2xl font-black text-slate-900 dark:text-white tracking-tighter tracking-tight">
                        {emailSent ? "Verify your Email" : "Create your Civic ID"}
                    </h1>
                    <p className="text-slate-500 dark:text-zinc-400 mt-2 text-sm font-medium">
                        {emailSent ? "Almost there! Check your inbox." : "Your universal gateway to schemes."}
                    </p>
                </div>

                {error && !emailSent && <div className="bg-rose-50 text-rose-600 text-sm font-bold p-4 rounded-xl mb-6 text-center border border-rose-200">{error}</div>}

                {emailSent ? (
                    <div className="text-center">
                        <div className="bg-emerald-50 dark:bg-emerald-500/10 border border-emerald-200 dark:border-emerald-500/20 rounded-2xl p-6 mb-6">
                            <h3 className="text-emerald-800 dark:text-emerald-400 font-bold mb-2 text-lg">Check Your Email</h3>
                            <p className="text-emerald-700 dark:text-emerald-500/80 text-sm leading-relaxed mb-4">
                                We've sent a 6-digit verification code to <strong>{formData.email}</strong>. Entering it below activates your account.
                            </p>
                        </div>
                        
                        <form onSubmit={handleVerifyOtp} className="space-y-4 mb-8">
                            <div>
                                <input
                                    type="text"
                                    value={otp}
                                    onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                                    className="w-full bg-slate-100 dark:bg-white/5 border border-slate-300 dark:border-white/20 rounded-xl px-4 py-4 text-center text-3xl font-mono tracking-[1em] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-orange-500/50"
                                    placeholder="••••••"
                                    required
                                    autoFocus
                                />
                            </div>
                            <button
                                type="submit"
                                disabled={verifying || otp.length < 6}
                                className="w-full bg-[#ea580c] hover:bg-[#c2410c] text-slate-900 dark:text-white font-bold text-[16px] py-4 rounded-xl transition-all disabled:bg-zinc-300 dark:disabled:bg-zinc-700 disabled:cursor-not-allowed shadow-xl shadow-orange-500/20"
                            >
                                {verifying ? 'Verifying...' : 'Complete Registration'}
                            </button>
                        </form>

                        <div className="flex items-center justify-center gap-4 border-t border-slate-200 dark:border-white/10 pt-6">
                            <p className="text-slate-500 dark:text-zinc-400 text-xs">Wrong email?</p>
                            <button
                                onClick={() => { setEmailSent(false); setFormData({ name: '', email: '', password: '', confirmPassword: '' }); setError(null); setOtp(''); }}
                                className="text-amber-600 hover:text-amber-700 font-bold text-xs"
                            >
                                Restart Registration
                            </button>
                        </div>
                    </div>
                ) : (
                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className={labelClass}>Name as in Aadhar</label>
                        <input type="text" value={formData.name} onChange={e => { if (/^[a-zA-Z\s]*$/.test(e.target.value)) setFormData({...formData, name: e.target.value}) }} className={inputClass} required />
                    </div>
                    <div>
                        <label className={labelClass}>Email Address</label>
                        <input type="email" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} className={inputClass} placeholder="citizen@example.com" required />
                    </div>
                    <div>
                        <label className={labelClass}>Password</label>
                        <div className="relative">
                            <input
                                type={showPassword ? 'text' : 'password'}
                                value={formData.password}
                                onChange={e => setFormData({...formData, password: e.target.value})}
                                className={`${inputClass} pr-12`}
                                placeholder="••••••••"
                                required
                                minLength="6"
                            />
                            <button
                                type="button"
                                onClick={() => setShowPassword(!showPassword)}
                                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 dark:text-zinc-400 hover:text-[#8A96A8] transition-colors p-1"
                                tabIndex={-1}
                            >
                                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                    </div>
                    <div>
                        <label className={labelClass}>Confirm Password</label>
                        <div className="relative">
                            <input
                                type={showConfirmPassword ? 'text' : 'password'}
                                value={formData.confirmPassword}
                                onChange={e => setFormData({...formData, confirmPassword: e.target.value})}
                                className={`${inputClass} pr-12`}
                                placeholder="••••••••"
                                required
                                minLength="6"
                            />
                            <button
                                type="button"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 dark:text-zinc-400 hover:text-[#8A96A8] transition-colors p-1"
                                tabIndex={-1}
                            >
                                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                    </div>

                    <button type="submit" disabled={loading} className="w-full bg-[#ea580c] hover:bg-[#c2410c] text-slate-900 dark:text-white font-bold text-[15px] py-4 rounded-xl transition-all disabled:bg-zinc-300 disabled:cursor-not-allowed mt-4 shadow-2xl shadow-black/5 dark:shadow-black/40">
                        {loading ? 'Processing...' : 'Verify & Continue'}
                    </button>
                    
                    <p className="text-center text-slate-500 dark:text-zinc-400 text-sm font-medium pt-2">
                        Already have an account? <Link to="/login" className="text-orange-500 hover:underline font-bold ml-1">Log in</Link>
                    </p>
                </form>
                )}
            </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
