import { Outlet, Link, useNavigate } from 'react-router-dom';

function Layout() {

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    window.location.href = '/';
  };

  return (
    <div>
      <nav style={{ background: '#1976d2', padding: '1rem', color: 'white' }}>
        <ul style={{ display: 'flex', gap: '1rem', listStyle: 'none', margin: 0 }}>
          <li><Link to="/" style={{ color: 'white', textDecoration: 'none' }}>Дашборд</Link></li>
          <li><Link to="/hosts" style={{ color: 'white', textDecoration: 'none' }}>Хосты</Link></li>
          <li><Link to="/agents" style={{ color: 'white', textDecoration: 'none' }}>Агенты</Link></li>
          <li style={{ marginLeft: 'auto' }}>
            <button onClick={handleLogout} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer' }}>
              Выйти
            </button>
          </li>
        </ul>
      </nav>
      <main style={{ padding: '1rem' }}>
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;