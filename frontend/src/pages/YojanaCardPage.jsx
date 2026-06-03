import React from 'react';
import QRCode from 'react-qr-code';
import { ArrowLeft, MapPin, Phone, Shield } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';

const YojanaCardPage = () => {
    const navigate = useNavigate();
    const { getActiveProfile } = useProfile();
    const profile = getActiveProfile();

    if (!profile) {
        return (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center min-h-[500px]">
                <Shield size={48} className="text-slate-300 mb-4" />
                <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-2">No Profile Found</h2>
                <p className="text-slate-500 mb-6">Please create or select a profile first to view your digital card.</p>
                <button
                    onClick={() => navigate('/profiles')}
                    className="px-6 py-3 bg-brand-500 text-white rounded-xl font-bold"
                >
                    Go to Profiles
                </button>
            </div>
        );
    }

    // Embed essential demographic points for rapid offline scanning
    const qrData = `SCHEMEWISE DIGITAL PASS
Name: ${profile.name}
DOB: ${profile.dob || 'N/A'} (Age: ${profile.age || 'N/A'})
Category: ${profile.category}
Income: ₹${profile.income}
Sector: ${profile.ruralUrban}, ${profile.state}
Occupation: ${profile.occupation || 'N/A'}`;

    return (
        <div className="flex-1 w-full max-w-2xl mx-auto py-8 px-4 sm:px-6 mt-8">
            <div className="flex items-center justify-between mb-6">
                <button
                    onClick={() => navigate(-1)}
                    className="print:hidden flex items-center gap-2 text-sm font-bold text-slate-500 dark:text-zinc-400 hover:text-slate-800 dark:hover:text-white uppercase tracking-wider transition-colors"
                >
                    <ArrowLeft size={16} /> Back
                </button>
                <button
                    onClick={() => window.print()}
                    className="flex items-center gap-2 text-sm font-bold bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-xl transition-colors shadow-lg shadow-orange-500/20 print:hidden"
                >
                    Print / Download Pass
                </button>
            </div>

            {/* Title Section */}
            <div className="text-center mb-8">
                <h1 className="text-3xl font-black text-slate-900 dark:text-white">Digital Yojana Card</h1>
                <p className="text-slate-500 dark:text-zinc-400 mt-2 text-sm font-medium">
                    Show this QR code at local government centers for instant eligibility verification.
                </p>
            </div>

            {/* Pass Container */}
            <div className="relative group">
                <div className="absolute -inset-1 bg-gradient-to-r from-orange-500 to-rose-500 rounded-[2.5rem] blur opacity-25 group-hover:opacity-40 transition duration-1000 group-hover:duration-200"></div>
                <div className="relative bg-white dark:bg-[#11151c] border border-slate-200 dark:border-white/10 rounded-[2rem] overflow-hidden shadow-2xl">
                    
                    {/* Header Strip */}
                    <div className="bg-slate-50 dark:bg-zinc-900 p-6 flex items-center justify-between border-b border-slate-100 dark:border-white/5">
                        <div className="flex flex-col">
                            <span className="text-xs font-bold uppercase tracking-widest text-slate-400 dark:text-zinc-500">Member Pass</span>
                            <span className="text-xl font-black text-slate-900 dark:text-white truncate">{profile.name}</span>
                        </div>
                        <Shield className="text-orange-500 opacity-20" size={48} />
                    </div>

                    {/* Content Section */}
                    <div className="p-8 pb-10 flex flex-col items-center">
                        <div className="bg-white p-4 rounded-2xl shadow-sm border border-slate-200 mb-8 inline-block mt-4">
                            <QRCode
                                value={qrData}
                                size={220}
                                level="M"
                                fgColor="#0F172A"
                                bgColor="#FFFFFF"
                            />
                        </div>

                        {/* Demographics Grid */}
                        <div className="w-full grid grid-cols-2 gap-y-6 gap-x-4">
                            <div className="flex flex-col gap-1">
                                <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400 dark:text-zinc-500">Date of Birth</span>
                                <span className="text-sm font-bold text-slate-800 dark:text-zinc-200">{profile.dob}</span>
                            </div>
                            <div className="flex flex-col gap-1">
                                <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400 dark:text-zinc-500">Income</span>
                                <span className="text-sm font-bold text-slate-800 dark:text-zinc-200">₹{parseInt(profile.income).toLocaleString('en-IN')}</span>
                            </div>
                            <div className="flex flex-col gap-1">
                                <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400 dark:text-zinc-500">Category</span>
                                <span className="text-sm font-bold text-slate-800 dark:text-zinc-200">{profile.category}</span>
                            </div>
                            <div className="flex flex-col gap-1">
                                <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400 dark:text-zinc-500">Occupation</span>
                                <span className="text-sm font-bold text-slate-800 dark:text-zinc-200">{profile.occupation || 'Not Specified'}</span>
                            </div>
                        </div>

                        <div className="w-full h-px bg-slate-100 dark:bg-white/5 my-6"></div>

                        <div className="w-full flex items-center justify-between px-2 text-slate-500 dark:text-zinc-400 text-xs font-semibold tracking-wide">
                            <div className="flex items-center gap-2">
                                <MapPin size={14} /> {profile.state}
                            </div>
                            {profile.phone && (
                                <div className="flex items-center gap-2">
                                    <Phone size={14} /> {profile.phone}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <p className="text-center text-xs text-slate-400 dark:text-zinc-500 mt-8 font-medium">
                This QR Code encodes your secure demographic footprint. It cannot be used for financial transactions.
            </p>
        </div>
    );
};

export default YojanaCardPage;
