import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';

function HostsList() {
  const [hosts, setHosts] = useState([]);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    api.get('/hosts')
      .then(res => setHosts(res.data))
      .catch(err => console.error(err));
  }, []);

  const filteredHosts = hosts.filter(host =>
    host.name.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div>
      <h1>Список хостов</h1>
      <input
        type="text"
        placeholder="Фильтр по имени..."
        value={filter}
        onChange={e => setFilter(e.target.value)}
        style={{ marginBottom: '1rem', padding: '0.5rem' }}
      />
      <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>Имя</th>
            <th>ОС</th>
            <th>Версия ядра</th>
            <th>Последнее обновление</th>
            <th>Устаревших пакетов</th>
          </tr>
        </thead>
        <tbody>
          {filteredHosts.map(host => (
            <tr key={host.id}>
              <td><Link to={`/hosts/${host.id}`}>{host.name}</Link></td>
              <td>{host.osPrettyName || host.osInfo}</td>
              <td>{host.kernelVersion}</td>
              <td>{new Date(host.lastUpdated).toLocaleString()}</td>
              <td>{host.outdatedCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default HostsList;