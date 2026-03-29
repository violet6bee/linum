import { Outlet, Link } from 'react-router-dom';

function Layout() {
  return (
    <div>
      <nav style={{ background: '#1976d2', padding: '1rem', color: 'white' }}>
        <ul style={{ display: 'flex', gap: '1rem', listStyle: 'none', margin: 0 }}>
          <li><Link to="/" style={{ color: 'white', textDecoration: 'none' }}>Дашборд</Link></li>
          <li><Link to="/hosts" style={{ color: 'white', textDecoration: 'none' }}>Хосты</Link></li>
        </ul>
      </nav>
      <main style={{ padding: '1rem' }}>
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;