import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Login from './components/Login';
import AgentsManagement from './components/AgentsManagement';
import Dashboard from './components/Dashboard';
import HostsList from './components/HostsList';
import HostDetails from './components/HostDetails';
import Layout from './components/Layout';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('adminToken'));

  useEffect(() => {
    const handleStorageChange = () => {
      setIsAuthenticated(!!localStorage.getItem('adminToken'));
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  if (!isAuthenticated) {
    return <Login onLogin={() => setIsAuthenticated(true)} />;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="hosts" element={<HostsList />} />
          <Route path="hosts/:id" element={<HostDetails />} />
          <Route path="agents" element={<AgentsManagement />} />
        </Route>
        <Route path="/login" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;