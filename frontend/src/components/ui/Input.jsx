import { forwardRef, useState } from 'react';
import { cn } from '../../lib/utils';
import { Eye, EyeOff } from 'lucide-react';

const Input = forwardRef(({ className, label, error, type = 'text', ...props }, ref) => {
    const [showPassword, setShowPassword] = useState(false);

    const isPassword = type === 'password';
    const inputType = isPassword ? (showPassword ? 'text' : 'password') : type;

    return (
        <div className="w-full flex flex-col gap-1.5 relative">
            {label && <label className="text-sm font-semibold text-slate-600 dark:text-zinc-300 ml-1">{label}</label>}
            <div className="relative">
                <input
                    ref={ref}
                    type={inputType}
                    className={cn(
                        "flex h-12 w-full rounded-xl border border-slate-200 dark:border-white/5 bg-transparent px-4 py-2 text-sm text-slate-800 dark:text-zinc-50 placeholder:text-slate-500 dark:text-zinc-400",
                        "focus:outline-none focus:ring-4 focus:ring-orange-500/20 focus:border-orange-500 transition-all",
                        "disabled:cursor-not-allowed disabled:opacity-50",
                        isPassword && "pr-10",
                        error && "border-red-500 focus:border-red-500 focus:ring-red-500/20",
                        className
                    )}
                    {...props}
                />
                {isPassword && (
                    <button
                        type="button"
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 dark:text-zinc-400 hover:text-slate-600 dark:text-zinc-300 p-1"
                        onClick={() => setShowPassword(!showPassword)}
                    >
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                )}
            </div>
            {error && <span className="text-xs text-red-500 font-medium ml-1">{error}</span>}
        </div>
    );
});
Input.displayName = 'Input';
export default Input;
