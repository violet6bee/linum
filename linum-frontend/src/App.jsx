import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './components/Dashboard';
import HostsList from './components/HostsList';
import HostDetails from './components/HostDetails';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="hosts" element={<HostsList />} />
          <Route path="hosts/:id" element={<HostDetails />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;