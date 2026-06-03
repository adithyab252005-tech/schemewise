import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';

export default function Button({
    className,
    variant = 'primary',
    size = 'md',
    isLoading,
    children,
    ...props
}) {
    const baseStyles = 'inline-flex items-center justify-center font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2 focus:ring-offset-zinc-950 disabled:opacity-50 disabled:pointer-events-none rounded-xl';

    const variants = {
        primary: 'bg-brand-600 text-slate-900 dark:text-white hover:bg-brand-500 shadow-[0_0_15px_#4f46e54d] hover:shadow-[0_0_25px_#4f46e580] border border-brand-500/50',
        secondary: 'bg-white dark:bg-zinc-900 text-slate-900 dark:text-white hover:bg-slate-200 dark:hover:bg-zinc-800 border border-slate-200 dark:border-white/5',
        outline: 'border border-zinc-600 text-slate-600 dark:text-zinc-300 hover:text-slate-900 dark:text-white hover:bg-white dark:bg-zinc-900',
        ghost: 'text-slate-600 dark:text-zinc-300 hover:text-slate-900 dark:text-white hover:bg-white dark:bg-white dark:bg-zinc-900/50',
        accent: 'bg-accent-500 text-slate-900 dark:text-white hover:bg-accent-400 shadow-[0_0_15px_#0ea5e94d] hover:shadow-[0_0_25px_#0ea5e980] border border-accent-400/50'
    };

    const sizes = {
        sm: 'h-9 px-4 text-sm',
        md: 'h-11 px-6 text-base',
        lg: 'h-14 px-8 text-lg rounded-2xl'
    };

    return (
        <motion.button
            whileHover={{ y: -2 }}
            whileTap={{ scale: 0.98 }}
            className={cn(baseStyles, variants[variant], sizes[size], className)}
            {...props}
        >
            {isLoading ? (
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-current" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
            ) : null}
            {children}
        </motion.button>
    );
}
