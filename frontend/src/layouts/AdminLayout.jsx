import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Database, PlusCircle, History, TestTube, LogOut } from 'lucide-react';
import { useProfile } from '../context/ProfileContext';

const AdminLayout = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { logout } = useProfile();

    const handleExit = () => {
        logout();
        navigate('/');
    };

    const sidebarLinks = [
        { name: 'Overview', path: '/admin', icon: LayoutDashboard },
        { name: 'Scheme Registry', path: '/admin/registry', icon: Database },
        { name: 'Add Scheme', path: '/admin/add', icon: PlusCircle },
        { name: 'Update Logs', path: '/admin/logs', icon: History },
        { name: 'SLM Testing', path: '/admin/slm-testing', icon: TestTube },
    ];

    return (
        <div className="app-layout">
            <aside className="sidebar">
                <div className="sidebar-header">
                    SchemeWise Admin
                </div>
                <nav className="sidebar-nav">
                    {sidebarLinks.map((link) => {
                        const Icon = link.icon;
                        return (
                            <Link
                                key={link.path}
                                to={link.path}
                                className={`sidebar-link ${location.pathname === link.path ? 'active' : ''}`}
                            >
                                <Icon size={20} />
                                {link.name}
                            </Link>
                        )
                    })}
                </nav>
                <div style={{ marginTop: 'auto', padding: '20px' }}>
                    <button onClick={handleExit} className="sidebar-link" style={{ color: 'var(--danger)', background: 'transparent', border: 'none', cursor: 'pointer', width: '100%', textAlign: 'left', outline: 'none' }}>
                        <LogOut size={20} />
                        Exit Admin
                    </button>
                </div>
            </aside>
            <main className="main-content">
                <div className="page-container" style={{ padding: '30px 40px' }}>
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default AdminLayout;
