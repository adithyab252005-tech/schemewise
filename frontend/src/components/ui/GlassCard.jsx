const GlassCard = ({ children, className = '', interactive = false, onClick }) => {
    return (
        <div 
            onClick={onClick}
            className={`rounded-3xl ${interactive ? 'glass-panel-interactive cursor-pointer' : 'glass-panel'} p-6 sm:p-8 ${className}`}
        >
            {children}
        </div>
    );
};

export default GlassCard;
