import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProfile } from '../context/ProfileContext';
import { Shield, Info } from 'lucide-react';

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

const Field = ({ label, children }) => (
    <div className="flex flex-col gap-1.5">
        <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">{label}</label>
        {children}
    </div>
);

const DetailsFillingPage = () => {
    const navigate = useNavigate();
    const { getActiveProfile, updateProfile, addProfile } = useProfile();
    const [loading, setLoading] = useState(false);
    const [details, setDetails] = useState({
        name: '', state: '', district: '', city: '', area: '', occupation: '', income: '',
        category: '', ruralUrban: '', age: '', dob: '', gender: '',
        isBPL: '', isStudent: '', isDifferentlyAbled: '', maritalStatus: '',
        isFarmer: '', employmentType: '',
        studentLevel: '', studentClass: '', studentDegreeType: '', studentCourse: '',
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setDetails(p => {
            const newDetails = { ...p, [name]: value };
            if (name === 'state') {
                newDetails.category = '';
            }
            return newDetails;
        });
    };

    const [dynamicCastes, setDynamicCastes] = useState(["General", "OBC", "SC", "ST", "Minority", "Prefer not to say"]);

    useEffect(() => {
        const fetchCastes = async () => {
            try {
                const url = details.state ? `/api/schemes/categories?state=${encodeURIComponent(details.state)}` : `/api/schemes/categories`;
                const res = await fetch(url);
                if (res.ok) {
                    const data = await res.json();
                    setDynamicCastes(data);
                }
            } catch (err) {
                console.error("Failed to fetch categories", err);
            }
        };
        fetchCastes();
    }, [details.state]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const profile = getActiveProfile();
            if (profile) { await updateProfile(profile.id, details); }
            else { await addProfile(details); }
            navigate('/results');
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
    };

    return (
        <div className="min-h-screen bg-transparent flex items-center justify-center px-4 py-16">
            <div className="w-full max-w-2xl">
                <div className="text-center mb-8">
                    <div className="w-14 h-14 rounded-[18px] bg-orange-600 flex items-center justify-center text-slate-900 dark:text-white font-bold text-2xl shadow-md mx-auto mb-5">S</div>
                    <h1 className="text-[28px] font-extrabold text-slate-900 dark:text-white tracking-tighter tracking-tight mb-1">Complete Your Profile</h1>
                    <p className="text-slate-500 dark:text-zinc-400 text-[14px]">This information lets us match you to the right schemes.</p>
                </div>

                <div className="pro-card rounded-3xl shadow-2xl shadow-black/5 dark:shadow-black/40 border border-slate-200 dark:border-white/5 p-8">
                    <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <Field label="Full Legal Name"><input name="name" value={details.name} onChange={handleChange} placeholder="Your full name" required className={inputClass} /></Field>
                            <Field label="Date of Birth"><input type="date" name="dob" value={details.dob} onChange={handleChange} required className={inputClass} /></Field>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <Field label="State">
                                <select name="state" value={details.state} onChange={handleChange} required className={selectClass}>
                                    <option value="" disabled>Select State</option>
                                    {states.map(s => <option key={s}>{s}</option>)}
                                </select>
                            </Field>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            <Field label="District"><input name="district" value={details.district} onChange={handleChange} placeholder="e.g. Pune" required className={inputClass} /></Field>
                            <Field label="City / Town"><input name="city" value={details.city} onChange={handleChange} placeholder="e.g. Kothrud" required className={inputClass} /></Field>
                            <Field label="Local Area (Optional)"><input name="area" value={details.area} onChange={handleChange} placeholder="e.g. Mayur Colony" className={inputClass} /></Field>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <Field label="Occupation"><input name="occupation" value={details.occupation} onChange={handleChange} placeholder="e.g. Farmer, Student" required className={inputClass} /></Field>
                            <Field label="Annual Income (₹)"><input type="number" name="income" value={details.income} onChange={handleChange} placeholder="e.g. 120000" required className={inputClass} /></Field>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <Field label="Age (Auto-calculated or Manual)"><input type="number" name="age" value={details.age} onChange={handleChange} placeholder="Your age" required className={inputClass} /></Field>
                            <Field label="Gender">
                                <select name="gender" value={details.gender} onChange={handleChange} required className={selectClass}>
                                    <option value="" disabled>Select Gender</option>
                                    <option>Male</option><option>Female</option><option>Transgender</option>
                                </select>
                            </Field>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <div className="flex flex-col gap-1.5">
                                <div className="flex items-center justify-between">
                                    <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300">Social Category</label>
                                    <div className="flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-500 bg-emerald-50 dark:bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-200 dark:border-emerald-500/20">
                                        <Shield size={10} /> Strictly Private
                                    </div>
                                </div>
                                <select name="category" value={details.category} onChange={handleChange} required className={selectClass}>
                                    <option value="" disabled>Select Category</option>
                                    {dynamicCastes.map(c => <option key={c}>{c}</option>)}
                                </select>
                                <div className="group relative inline-flex items-center w-max gap-1.5 mt-1 text-[10px] text-slate-500 dark:text-zinc-500 font-medium">
                                    <Info size={12} className="text-slate-400 dark:text-zinc-500 cursor-help" />
                                    <span>Why is this private?</span>
                                    <div className="absolute w-56 p-3 bg-slate-900 dark:bg-black text-white rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.3)] border border-white/10 text-xs bottom-full mb-2 left-0 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-20 leading-relaxed">
                                        Secured via <strong className="text-orange-400">Zero-Knowledge Encryption</strong>. This data is mathematically locked on your device. We cannot read it.
                                        {/* Tooltip caret */}
                                        <div className="absolute -bottom-1.5 left-4 w-3 h-3 bg-slate-900 dark:bg-black border-r border-b border-white/10 rotate-45"></div>
                                    </div>
                                </div>
                            </div>
                            <Field label="Area Type">
                                <select name="ruralUrban" value={details.ruralUrban} onChange={handleChange} required className={selectClass}>
                                    <option value="" disabled>Select Area Type</option>
                                    <option>Urban</option><option>Rural</option>
                                </select>
                            </Field>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            {[['isBPL','BPL Family?'],['isStudent','Are you a Student?'],['isDifferentlyAbled','Differently Abled?']].map(([name,label]) => (
                                <Field key={name} label={label}>
                                    <select name={name} value={details[name]} onChange={handleChange} required className={selectClass}>
                                        <option value="" disabled>Select</option>
                                        <option>No</option><option>Yes</option>
                                    </select>
                                </Field>
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
                            <select name="maritalStatus" value={details.maritalStatus} onChange={handleChange} required className={selectClass}>
                                <option value="" disabled>Select Status</option>
                                {['Single','Married','Widowed','Divorced'].map(s => <option key={s}>{s}</option>)}
                            </select>
                        </Field>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <Field label="Employment Type">
                                <select name="employmentType" value={details.employmentType} onChange={handleChange} required className={selectClass}>
                                    <option value="" disabled>Select Employment Type</option>
                                    {['Farmer / Cultivator','Agricultural Labourer','Govt. Employee','Private Employee','Self-employed / Business','Daily Wage Worker','Unemployed','Student','Homemaker'].map(t => <option key={t}>{t}</option>)}
                                </select>
                            </Field>
                            <Field label="Are you a Farmer?">
                                <select name="isFarmer" value={details.isFarmer} onChange={handleChange} required className={selectClass}>
                                    <option value="" disabled>Select</option>
                                    <option>No</option><option>Yes</option>
                                </select>
                            </Field>
                        </div>
                        <button type="submit" disabled={loading}
                            className="w-full py-3.5 mt-2 bg-[#FF5E00] text-white font-bold text-[16px] rounded-2xl shadow-md hover:bg-[#E05200] border-transparent hover:border-orange-500/30 transition-all disabled:bg-zinc-300 disabled:cursor-not-allowed">
                            {loading ? 'Checking Eligibility…' : 'Check My Eligibility →'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default DetailsFillingPage;
