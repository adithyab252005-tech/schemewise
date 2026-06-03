const StatusBadge = ({ status }) => {
    let badgeClass = 'badge-yellow'; // default partial
    let label = status;

    const lowerStatus = (status || '').toLowerCase();

    if (lowerStatus.includes('eligible') && !lowerStatus.includes('not') && !lowerStatus.includes('partial')) {
        badgeClass = 'badge-green';
    } else if (lowerStatus.includes('not') || lowerStatus.includes('deprecated') || lowerStatus.includes('inactive')) {
        badgeClass = 'badge-red';
    } else if (lowerStatus.includes('active')) {
        badgeClass = 'badge-green';
    }

    return (
        <span className={`badge ${badgeClass}`}>{label || 'Unknown'}</span>
    );
};

export default StatusBadge;
