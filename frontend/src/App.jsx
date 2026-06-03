import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import PublicLayout from './layouts/PublicLayout';
import PrivateRoute from './components/PrivateRoute';
import DashboardLayout from './layouts/DashboardLayout';
import AndroidBackButtonListener from './components/AndroidBackButtonListener';

// Public Pages (Placeholders for now)
import LandingPage from './pages/LandingPage';
import ProfilePage from './pages/ProfilePage';
import DashboardPage from './pages/DashboardPage';
import SchemeResultsPage from './pages/SchemeResultsPage';
import SchemeDetailPage from './pages/SchemeDetailPage';
import SchemeAIDetailsPage from './pages/SchemeAIDetailsPage';
import PrintSchemePage from './pages/PrintSchemePage';
import SimulatorPage from './pages/SimulatorPage';
import ExploreSchemesPage from './pages/ExploreSchemesPage';
import UpdatesPage from './pages/UpdatesPage';
import BotAgentPage from './pages/BotAgentPage';
import SavedSchemesPage from './pages/SavedSchemesPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

import CivicReadinessPage from './pages/CivicReadinessPage';
import ComboBenefitsPage from './pages/ComboBenefitsPage';
import CentersMapPage from './pages/CentersMapPage';
import ComparePage from './pages/ComparePage';
import YojanaCardPage from './pages/YojanaCardPage';

// New Onboarding Pages
import WelcomePage from './pages/WelcomePage';
import DetailsFillingPage from './pages/DetailsFillingPage';
import EligibilityResultsPage from './pages/EligibilityResultsPage';
import JourneyIntroPage from './pages/JourneyIntroPage';
import SettingsPage from './pages/SettingsPage';

// Real Admin Pages
import AdminLayout from './pages/admin/AdminLayout';
import SchemeManagement from './pages/admin/SchemeManagement';
import AnalyticsDashboard from './pages/admin/AnalyticsDashboard';
import AIControlCenter from './pages/admin/AIControlCenter';
import UserManagement from './pages/admin/UserManagement';

function App() {
  return (
    <>
      <div className="bg-stunning"></div>
      <Router>
        <AndroidBackButtonListener />
        <Routes>
          {/* Public Routes */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
        </Route>

        {/* Protected Application Routes */}
        <Route element={<PrivateRoute />}>
          <Route path="/scheme/:id/print" element={<PrintSchemePage />} />
          <Route element={<DashboardLayout />}>
            <Route path="/welcome" element={<WelcomePage />} />
            <Route path="/onboarding/details" element={<DetailsFillingPage />} />
            <Route path="/onboarding/results" element={<EligibilityResultsPage />} />
            <Route path="/onboarding/journey" element={<JourneyIntroPage />} />

            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/results" element={<SchemeResultsPage />} />
            <Route path="/scheme/:id" element={<SchemeDetailPage />} />
            <Route path="/scheme/:id/ai-details" element={<SchemeAIDetailsPage />} />
            <Route path="/simulator" element={<SimulatorPage />} />
            <Route path="/explore" element={<ExploreSchemesPage />} />
            <Route path="/saved" element={<SavedSchemesPage />} />
            <Route path="/civic" element={<CivicReadinessPage />} />
            <Route path="/combo" element={<ComboBenefitsPage />} />
            <Route path="/bot" element={<BotAgentPage />} />
            <Route path="/centers" element={<CentersMapPage />} />
            <Route path="/updates" element={<UpdatesPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/compare" element={<ComparePage />} />
            <Route path="/yojana-card" element={<YojanaCardPage />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Route>
        </Route>

        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AnalyticsDashboard />} />
          <Route path="/admin/analytics" element={<AnalyticsDashboard />} />
          <Route path="/admin/schemes" element={<SchemeManagement />} />
          <Route path="/admin/ai-control" element={<AIControlCenter />} />
          <Route path="/admin/users" element={<UserManagement />} />
        </Route>
      </Routes>
    </Router>
    </>
  );
}

export default App;
