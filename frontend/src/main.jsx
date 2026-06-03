import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import './i18n';
import { ProfileProvider } from './context/ProfileContext';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }
  render() {
    if (this.state.hasError) {
      return <div style={{ color: 'red', padding: '2rem', background: 'white' }}><h1>FATAL CRASH</h1><pre style={{ whiteSpace: 'pre-wrap' }}>{this.state.error.stack}</pre></div>;
    }
    return this.props.children;
  }
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ErrorBoundary>
      <ProfileProvider>
        <App />
      </ProfileProvider>
    </ErrorBoundary>
  </React.StrictMode>,
)
