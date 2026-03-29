import { useEffect, useState } from 'react';
import api from '../api';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';

function Dashboard() {
  const [stats, setStats] = useState({ totalHosts: 0, outdatedHosts: 0, osDistribution: [] });
  const [recentHosts, setRecentHosts] = useState([]);

  useEffect(() => {
    // Получаем статистику
    api.get('/stats')
      .then(res => setStats(res.data))
      .catch(err => console.error(err));

    // Получаем последние активные хосты (первые 5)
    api.get('/hosts')
      .then(res => setRecentHosts(res.data.slice(0, 5)))
      .catch(err => console.error(err));
  }, []);

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

  return (
    <div>
      <h1>Панель управления</h1>
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
        <div style={{ border: '1px solid #ccc', padding: '1rem', borderRadius: '8px' }}>
          <h3>Всего хостов</h3>
          <p style={{ fontSize: '2rem' }}>{stats.totalHosts}</p>
        </div>
        <div style={{ border: '1px solid #ccc', padding: '1rem', borderRadius: '8px' }}>
          <h3>Требуют обновлений</h3>
          <p style={{ fontSize: '2rem' }}>{stats.outdatedHosts}</p>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
        <div style={{ flex: 1 }}>
          <h3>Распределение по ОС</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={stats.osDistribution}
                dataKey="count"
                nameKey="os"
                cx="50%"
                cy="50%"
                outerRadius={80}
                fill="#8884d8"
                label
              >
                {stats.osDistribution.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div style={{ flex: 1 }}>
          <h3>Последние активные хосты</h3>
          <ul>
            {recentHosts.map(host => (
              <li key={host.id}>{host.name} ({host.osPrettyName || host.name}) – обновлений: {host.outdatedCount || 0}</li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;