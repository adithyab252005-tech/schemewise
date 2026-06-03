import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';
import { useSchemes } from '../hooks/useSchemes';
import { Users, Plus, ShieldCheck, Trash2, Zap, Shield, Info } from 'lucide-react';

const states = [
    'Andaman and Nicobar Islands', 'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar',
    'Chandigarh', 'Chhattisgarh', 'Dadra and Nagar Haveli and Daman and Diu', 'Delhi', 'Goa',
    'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jammu and Kashmir', 'Jharkhand', 'Karnataka',
    'Kerala', 'Ladakh', 'Lakshadweep', 'Madhya Pradesh', 'Maharashtra', 'Manipur', 'Meghalaya',
    'Mizoram', 'Nagaland', 'Odisha', 'Puducherry', 'Punjab', 'Rajasthan', 'Sikkim',
    'Tamil Nadu', 'Telangana', 'Tripura', 'Uttar Pradesh', 'Uttarakhand', 'West Bengal'
];
const inputClass = "w-full px-4 py-3.5 pro-card rounded-2xl text-sm text-slate-800 dark:text-zinc-100 focus:ring-2 focus:ring-orange-500/30/20 focus:border-slate-200 dark:border-white/5 outline-none transition-all placeholder:text-slate-600 dark:text-zinc-300 shadow-2xl shadow-black/5 dark:shadow-black/40";
const selectClass = "w-full px-4 py-3.5 pro-card rounded-2xl text-sm text-slate-800 dark:text-zinc-100 focus:ring-2 focus:ring-orange-500/30/20 focus:border-slate-200 dark:border-white/5 outline-none transition-all shadow-2xl shadow-black/5 dark:shadow-black/40 appearance-none";

const Field = ({ label, children }) => <div className="flex flex-col gap-1.5"><label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">{label}</label>{children}</div>;

const ProfilePage = () => {
    const navigate = useNavigate();
    const { evaluateProfile } = useSchemes();
    const { getActiveProfile, updateProfile } = useProfile();
    const profile = getActiveProfile();
    const [details, setDetails] = useState({
        name: '', email: '', state: '', district: '', city: '', area: '',
        occupation: '', income: '', category: '', ruralUrban: '', age: '', dob: '', gender: '',
        isBPL: 'No', isStudent: 'No', isDifferentlyAbled: 'No', maritalStatus: 'Single',
        isFarmer: 'No', employmentType: '',
        studentLevel: '', studentClass: '', studentDegreeType: '', studentCourse: '',
    });
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);
    
    // Household Welfare Maximizer State
    const [familyMembers, setFamilyMembers] = useState(() => {
        try {
            const savedData = localStorage.getItem(`family_${profile?.id || 'guest'}`);
            return savedData ? JSON.parse(savedData) : [];
        } catch(e) { return []; }
    });
    const [showAddMember, setShowAddMember] = useState(false);
    const emptyMember = { name: '', age: '', income: '', relation: 'Spouse', gender: 'Male', category: 'General', occupation: '', state: 'Tamil Nadu', ruralUrban: 'Urban', isStudent: 'No', isBPL: 'No', isDifferentlyAbled: 'No' };
    const [newMember, setNewMember] = useState(emptyMember);
    const [evaluatingId, setEvaluatingId] = useState(null);

    useEffect(() => {
        const profile = getActiveProfile();
        if (profile) setDetails({
            name:               profile.name || '',
            email:              profile.email || '',
            state:              profile.state || '',
            district:           profile.district || '',
            city:               profile.city || '',
            area:               profile.area || '',
            occupation:         profile.occupation || '',
            income:             profile.income || '',
            category:           profile.category || '',
            ruralUrban:         profile.ruralUrban || profile.rural_urban || '',
            age:                profile.age || '',
            dob:                profile.dob || '',
            gender:             profile.gender || '',
            isBPL:              profile.isBPL || profile.is_bpl || 'No',
            isStudent:          profile.isStudent || profile.is_student || 'No',
            isDifferentlyAbled: profile.isDifferentlyAbled || profile.is_differently_abled || 'No',
            maritalStatus:      profile.maritalStatus || profile.marital_status || 'Single',
            isFarmer:           profile.isFarmer || profile.is_farmer || 'No',
            employmentType:     profile.employmentType || profile.employment_type || '',
            // Student detail fields
            studentLevel:       profile.studentLevel || profile.student_level || '',
            studentClass:       profile.studentClass || profile.student_class || '',
            studentDegreeType:  profile.studentDegreeType || profile.student_degree_type || '',
            studentCourse:      profile.studentCourse || profile.student_course || '',
        });
    }, [getActiveProfile]);

    useEffect(() => {
        const profile = getActiveProfile();
        if (profile?.id) {
            try {
                const savedData = localStorage.getItem(`family_${profile.id}`);
                if (savedData) setFamilyMembers(JSON.parse(savedData));
            } catch(e) {}
        }
    }, [getActiveProfile]);

    const handleChange = (e) => setDetails(p => ({ ...p, [e.target.name]: e.target.value }));
    const toggleDocument = (docName) => {
        setDetails(p => ({
            ...p,
            hasDocuments: p.hasDocuments.includes(docName)
                ? p.hasDocuments.filter(d => d !== docName)
                : [...p.hasDocuments, docName]
        }));
    };
    const handleSubmit = async (e) => {
        e.preventDefault(); setSaving(true); setSaved(false);
        try { const profile = getActiveProfile(); if (profile) await updateProfile(profile.id, details); setSaved(true); setTimeout(() => setSaved(false), 3000); }
        catch (err) { console.error(err); } finally { setSaving(false); }
    };

    const handleAddFamily = (e) => {
        e.preventDefault();
        if (!newMember.name) return;
        // Compute a basic bonus score based on filled fields
        let bonus = 1;
        if (newMember.isBPL === 'Yes') bonus += 2;
        if (newMember.isStudent === 'Yes') bonus += 1;
        if (newMember.isDifferentlyAbled === 'Yes') bonus += 2;
        if (['SC','ST','OBC','Minority'].includes(newMember.category)) bonus += 1;
        
        const updated = [...familyMembers, { id: Date.now(), ...newMember, newSchemes: bonus }];
        setFamilyMembers(updated);
        if (profile?.id) localStorage.setItem(`family_${profile.id}`, JSON.stringify(updated));
        
        setNewMember(emptyMember);
        setShowAddMember(false);
    };

    const handleRemoveFamily = (id) => {
        const updated = familyMembers.filter(m => m.id !== id);
        setFamilyMembers(updated);
        if (profile?.id) localStorage.setItem(`family_${profile.id}`, JSON.stringify(updated));
    };

    const handleEvaluateMember = async (member) => {
        setEvaluatingId(member.id);
        const results = await evaluateProfile(member);
        if (results) {
            navigate('/results', { state: { results } });
        }
        setEvaluatingId(null);
    };

    const totalHouseholdSchemes = 12 + familyMembers.reduce((acc, curr) => acc + curr.newSchemes, 0);

    return (
        <div className="flex-1 w-full max-w-5xl mx-auto py-8 px-4 sm:px-6">
            <div className="mb-7">
                <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white tracking-tighter mb-1">Account & Household</h1>
                <p className="text-slate-500 dark:text-zinc-400 text-sm font-medium">Manage your civic identity and family benefits.</p>
            </div>
            
            <div className="flex flex-col lg:flex-row gap-6">
                {/* Left Sidebar */}
                <div className="w-full lg:w-80 flex flex-col gap-6">
                    <div className="pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 p-6 text-center">
                        <div className="w-20 h-20 rounded-2xl bg-orange-600 mx-auto flex items-center justify-center text-3xl text-slate-900 dark:text-white font-extrabold mb-4 shadow-md">
                            {details.name ? details.name.charAt(0).toUpperCase() : 'U'}
                        </div>
                        <h3 className="font-bold text-slate-900 dark:text-white tracking-tight text-lg mb-1">{details.name || 'Citizen'}</h3>
                        <p className="text-slate-500 dark:text-zinc-400 font-medium text-xs mb-4">ID: {getActiveProfile()?.id || '000000'}</p>
                        
                        <div className="text-sm border-t border-slate-200 dark:border-white/5 pt-4 flex flex-col gap-2">
                            <div className="flex justify-between">
                                <span className="text-slate-500 dark:text-zinc-400 font-medium">Profile Completeness</span>
                                <span className="text-emerald-600 font-bold">100%</span>
                            </div>
                        </div>
                    </div>

                    {/* Household Selector */}
                    <div className="bg-amber-50 rounded-3xl border border-amber-200 shadow-2xl shadow-black/5 dark:shadow-black/40 p-6">
                        <div className="flex items-center gap-2 mb-4">
                            <Users size={20} className="text-amber-600" />
                            <h3 className="font-bold text-slate-900 dark:text-white tracking-tight">Household & Family Link</h3>
                        </div>
                        <p className="text-xs font-medium text-slate-500 dark:text-zinc-400 max-w-[200px] leading-relaxed">Combine family profiles to unlock special combined benefits.</p>
                        
                        <div className="mb-4">
                            <span className="text-3xl font-black text-amber-600">{totalHouseholdSchemes}</span>
                            <span className="text-xs font-bold uppercase tracking-wider text-amber-500 ml-2">Total Combined Schemes</span>
                        </div>
                        <button onClick={() => navigate('/combo')} className="w-full mt-4 bg-orange-500 hover:bg-orange-600 text-white font-bold py-3.5 rounded-xl shadow-[0_8px_24px_rgba(249,115,22,0.3)] transition-all text-sm tracking-wide">
                            View Household Combos & Benefits →
                        </button>

                        <div className="space-y-2 mb-4">
                            {familyMembers.map((m) => (
                                <div key={m.id} className="pro-card border border-amber-100 rounded-xl p-3 flex justify-between items-center shadow-2xl shadow-black/5 dark:shadow-black/40">
                                    <div className="flex-1 truncate pr-2">
                                        <p className="text-sm font-bold text-slate-900 dark:text-white tracking-tight truncate">{m.name}</p>
                                        <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500 dark:text-zinc-400">{m.relation}</p>
                                    </div>
                                    <div className="flex items-center gap-1.5 shrink-0">
                                        <button 
                                            onClick={() => handleEvaluateMember(m)}
                                            disabled={evaluatingId === m.id}
                                            className="text-xs font-bold text-orange-500 bg-white dark:bg-zinc-900 hover:bg-orange-100 border border-orange-200 px-3 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1 shadow-2xl shadow-black/5 dark:shadow-black/40 disabled:opacity-50"
                                            title="Check Eligibility"
                                        >
                                            {evaluatingId === m.id ? 'Checking...' : <><Zap size={14} /> Evaluate</>}
                                        </button>
                                        <button onClick={() => handleRemoveFamily(m.id)} className="text-slate-500 dark:text-zinc-400 hover:text-rose-500 hover:bg-rose-50 p-2 rounded-lg transition-colors cursor-pointer border border-transparent hover:border-rose-200" title="Remove">
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {!showAddMember ? (
                            <button onClick={() => setShowAddMember(true)} className="w-full flex items-center justify-center gap-2 py-3 pro-card rounded-xl text-slate-500 dark:text-zinc-400 font-bold text-sm hover:border-slate-200 dark:border-white/5 transition-colors shadow-2xl shadow-black/5 dark:shadow-black/40">
                                <Plus size={16} /> Add Member
                            </button>
                        ) : (
                            <form onSubmit={handleAddFamily} className="pro-card p-4 rounded-xl border border-amber-300 shadow-2xl shadow-black/5 dark:shadow-black/40 mt-3 flex flex-col gap-3">
                                <h4 className="font-bold text-slate-900 dark:text-white tracking-tight text-sm">Add Family Member</h4>
                                {/* Name & Relation */}
                                <div className="grid grid-cols-2 gap-2">
                                    <input required type="text" placeholder="Full Name" value={newMember.name} onChange={(e) => setNewMember({...newMember, name: e.target.value})} className={`${inputClass} !py-2 text-xs`} />
                                    <select value={newMember.relation} onChange={(e) => setNewMember({...newMember, relation: e.target.value})} className={`${selectClass} !py-2 text-xs`}>
                                        <option>Spouse</option><option>Child</option><option>Parent</option><option>Sibling</option><option>Other</option>
                                    </select>
                                </div>
                                {/* Age & Gender */}
                                <div className="grid grid-cols-2 gap-2">
                                    <input required type="number" placeholder="Age" value={newMember.age} onChange={(e) => setNewMember({...newMember, age: e.target.value})} className={`${inputClass} !py-2 text-xs`} />
                                    <select value={newMember.gender} onChange={(e) => setNewMember({...newMember, gender: e.target.value})} className={`${selectClass} !py-2 text-xs`}>
                                        <option>Male</option><option>Female</option><option>Transgender</option>
                                    </select>
                                </div>
                                {/* Income & Category */}
                                <div className="grid grid-cols-2 gap-2">
                                    <input required type="number" placeholder="Annual Income (₹)" value={newMember.income} onChange={(e) => setNewMember({...newMember, income: e.target.value})} className={`${inputClass} !py-2 text-xs`} />
                                    <select value={newMember.category} onChange={(e) => setNewMember({...newMember, category: e.target.value})} className={`${selectClass} !py-2 text-xs`}>
                                        {['General','OBC','SC','ST','Minority'].map(c => <option key={c}>{c}</option>)}
                                    </select>
                                </div>
                                {/* Occupation */}
                                <input type="text" placeholder="Occupation (e.g. Farmer, Student)" value={newMember.occupation} onChange={(e) => setNewMember({...newMember, occupation: e.target.value})} className={`${inputClass} !py-2 text-xs`} />
                                {/* State & Area */}
                                <div className="grid grid-cols-2 gap-2">
                                    <select value={newMember.state} onChange={(e) => setNewMember({...newMember, state: e.target.value})} className={`${selectClass} !py-2 text-xs`}>
                                        {states.map(s => <option key={s}>{s}</option>)}
                                    </select>
                                    <select value={newMember.ruralUrban} onChange={(e) => setNewMember({...newMember, ruralUrban: e.target.value})} className={`${selectClass} !py-2 text-xs`}>
                                        <option>Urban</option><option>Rural</option>
                                    </select>
                                </div>
                                {/* Special flags */}
                                <div className="grid grid-cols-3 gap-2">
                                    {[['isStudent','Student?'],['isBPL','BPL?'],['isDifferentlyAbled','Disabled?']].map(([key, lbl]) => (
                                        <div key={key} className="flex flex-col gap-1">
                                            <span className="text-[10px] font-bold text-slate-500 dark:text-zinc-400 uppercase tracking-wider">{lbl}</span>
                                            <select value={newMember[key]} onChange={(e) => setNewMember({...newMember, [key]: e.target.value})} className={`${selectClass} !py-1.5 text-xs`}>
                                                <option>No</option><option>Yes</option>
                                            </select>
                                        </div>
                                    ))}
                                </div>
                                <div className="flex gap-2 mt-1">
                                    <button type="submit" className="flex-1 bg-orange-600 hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 text-slate-900 dark:text-white text-xs font-bold py-2.5 rounded-lg transition-colors">Add Member</button>
                                    <button type="button" onClick={() => { setShowAddMember(false); setNewMember(emptyMember); }} className="flex-1 bg-orange-600 hover:bg-zinc-200 text-slate-500 dark:text-zinc-400 text-xs font-bold py-2.5 rounded-lg transition-colors">Cancel</button>
                                </div>
                            </form>
                        )}
                    </div>
                </div>

                <div className="flex-1">
                    <div className="pro-card rounded-3xl border border-slate-200 dark:border-white/5 shadow-2xl shadow-black/5 dark:shadow-black/40 p-6 sm:p-8">
                        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                            <div className="flex items-center gap-2 border-b border-slate-200 dark:border-white/5 pb-4">
                                <ShieldCheck size={20} className="text-orange-500" />
                                <h3 className="text-slate-900 dark:text-white font-bold text-base">Primary Demographic Details</h3>
                            </div>
                            
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <Field label="Full Name"><input name="name" value={details.name} onChange={handleChange} required className={inputClass} /></Field>
                                <Field label="Email"><input type="email" name="email" value={details.email} onChange={handleChange} disabled placeholder="Managed by auth" className={`${inputClass} opacity-60 bg-transparent`} /></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <Field label="State">
                                    <select name="state" value={details.state} onChange={handleChange} className={selectClass}>
                                        <option value="" disabled>Select State</option>
                                        {states.map(s => <option key={s}>{s}</option>)}
                                    </select>
                                </Field>
                                <Field label="District"><input name="district" value={details.district} onChange={handleChange} placeholder="e.g. Pune" required className={inputClass} /></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <Field label="City / Town"><input name="city" value={details.city} onChange={handleChange} placeholder="e.g. Kothrud" required className={inputClass} /></Field>
                                <Field label="Local Area (Optional)"><input name="area" value={details.area} onChange={handleChange} placeholder="e.g. Mayur Colony" className={inputClass} /></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <Field label="Area Type"><select name="ruralUrban" value={details.ruralUrban} onChange={handleChange} className={selectClass}><option>Urban</option><option>Rural</option></select></Field>
                                <Field label="Occupation"><input name="occupation" value={details.occupation} onChange={handleChange} placeholder="e.g. Farmer, Student" className={inputClass} /></Field>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <Field label="Annual Income (₹)"><input type="number" name="income" value={details.income} onChange={handleChange} required className={inputClass} /></Field>
                                
                                <div className="flex flex-col gap-1.5">
                                    <div className="flex items-center justify-between">
                                        <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">Social Category</label>
                                        <div className="flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-500 bg-emerald-50 dark:bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-200 dark:border-emerald-500/20">
                                            <Shield size={10} /> Strictly Private
                                        </div>
                                    </div>
                                    <select name="category" value={details.category} onChange={handleChange} className={selectClass}>
                                        {['General','OBC','SC','ST','Minority','Prefer not to say'].map(c => <option key={c}>{c}</option>)}
                                    </select>
                                </div>
                            </div>
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
                                <Field label="Date of Birth"><input type="date" name="dob" value={details.dob} onChange={handleChange} required className={inputClass} /></Field>
                                <Field label="Age"><input type="number" name="age" value={details.age} onChange={handleChange} required className={inputClass} /></Field>
                                <Field label="Gender"><select name="gender" value={details.gender} onChange={handleChange} className={selectClass}><option>Male</option><option>Female</option><option>Transgender</option></select></Field>
                            </div>
                            
                            {/* --- New Employment & Student Block --- */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-5 border-t border-slate-200 dark:border-white/5 pt-4">
                                <Field label="Employment Type">
                                    <select name="employmentType" value={details.employmentType} onChange={handleChange} required className={selectClass}>
                                        <option value="" disabled>Select Employment Type</option>
                                        {['Farmer / Cultivator','Agricultural Labourer','Govt. Employee','Private Employee','Self-employed / Business','Daily Wage Worker','Unemployed','Student','Homemaker'].map(t => <option key={t}>{t}</option>)}
                                    </select>
                                </Field>
                                <Field label="Are you a Farmer?">
                                    <select name="isFarmer" value={details.isFarmer} onChange={handleChange} required className={selectClass}>
                                        <option value="" disabled>Select</option><option>No</option><option>Yes</option>
                                    </select>
                                </Field>
                            </div>

                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 border-t border-slate-200 dark:border-white/5 pt-4">
                                {[['isStudent','Student?'],['isDifferentlyAbled','Differently Abled?'],['isBPL','BPL?']].map(([n, l]) => (
                                    <Field key={n} label={l}><select name={n} value={details[n]} onChange={handleChange} className={selectClass}><option>No</option><option>Yes</option></select></Field>
                                ))}
                            </div>

                            {/* ── Conditional Student Detail Fields ── */}
                            {details.isStudent === 'Yes' && (
                                <div className="border border-blue-200 dark:border-blue-500/20 bg-blue-50 dark:bg-blue-500/5 rounded-2xl p-5 flex flex-col gap-4">
                                    <div className="text-xs font-black uppercase tracking-widest text-blue-600 dark:text-blue-400">📚 Student Details</div>
                                    <Field label="Study Level">
                                        <select name="studentLevel" value={details.studentLevel} onChange={handleChange} required className={selectClass}>
                                            <option value="" disabled>Select Level</option>
                                            <option value="school">School (Class 1–12)</option>
                                            <option value="college">College (UG / Diploma)</option>
                                            <option value="university">University (PG / PhD)</option>
                                        </select>
                                    </Field>

                                    {details.studentLevel === 'school' && (
                                        <Field label="Which Class / Grade?">
                                            <select name="studentClass" value={details.studentClass} onChange={handleChange} required className={selectClass}>
                                                <option value="" disabled>Select Class</option>
                                                <option value="1-5">Class 1 – 5 (Primary)</option>
                                                <option value="6-8">Class 6 – 8 (Middle)</option>
                                                <option value="9-10">Class 9 – 10 (Secondary / Matric)</option>
                                                <option value="11-12">Class 11 – 12 (Higher Secondary / +2)</option>
                                            </select>
                                        </Field>
                                    )}

                                    {(details.studentLevel === 'college' || details.studentLevel === 'university') && (
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                            <Field label="Degree Type">
                                                <select name="studentDegreeType" value={details.studentDegreeType} onChange={handleChange} required className={selectClass}>
                                                    <option value="" disabled>Select Degree</option>
                                                    <option value="diploma">Diploma / Certificate</option>
                                                    <option value="ug">UG – Bachelor's (BA / BSc / BTech…)</option>
                                                    <option value="pg">PG – Master's (MA / MSc / MTech…)</option>
                                                    <option value="phd">PhD / Doctoral / Research</option>
                                                    <option value="professional">Professional (MBBS / LLB / CA / MBBS)</option>
                                                </select>
                                            </Field>
                                            <Field label="Course / Stream">
                                                <select name="studentCourse" value={details.studentCourse} onChange={handleChange} required className={selectClass}>
                                                    <option value="" disabled>Select Course / Stream</option>
                                                    <option value="engineering">Engineering &amp; Technology</option>
                                                    <option value="medical">Medical &amp; Health Sciences</option>
                                                    <option value="science">Pure Sciences (BSc / MSc)</option>
                                                    <option value="arts">Arts &amp; Humanities</option>
                                                    <option value="commerce">Commerce &amp; Management</option>
                                                    <option value="law">Law (LLB / LLM)</option>
                                                    <option value="agriculture">Agriculture &amp; Allied Sciences</option>
                                                    <option value="education">Education / B.Ed / M.Ed</option>
                                                    <option value="pharmacy">Pharmacy</option>
                                                    <option value="nursing">Nursing / Paramedical</option>
                                                    <option value="polytechnic">Polytechnic / ITI</option>
                                                    <option value="other">Other</option>
                                                </select>
                                            </Field>
                                        </div>
                                    )}
                                </div>
                            )}

                            <Field label="Marital Status">
                                <select name="maritalStatus" value={details.maritalStatus} onChange={handleChange} className={selectClass}>{['Single','Married','Widowed','Divorced'].map(s => <option key={s}>{s}</option>)}</select>
                            </Field>

                            <div className="pt-6 mt-2 border-t border-slate-200 dark:border-white/5 flex justify-end items-center gap-4">
                                {saved && <span className="text-emerald-600 text-sm font-bold bg-emerald-50 px-3 py-1.5 rounded-lg border border-emerald-100">✓ Saved Sync</span>}
                                <button type="submit" disabled={saving} className="px-8 py-3.5 bg-orange-600 text-slate-900 dark:text-white font-bold rounded-2xl hover:bg-white dark:bg-zinc-900 border-transparent hover:border-orange-500/30 transition-all disabled:bg-zinc-300 shadow-md">
                                    {saving ? 'Saving…' : 'Update Profile'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
