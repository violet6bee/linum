import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api';

function HostDetails() {
  const { id } = useParams();
  const [host, setHost] = useState(null);
  const [activeTab, setActiveTab] = useState('packages');

  useEffect(() => {
    api.get(`/hosts/${id}`)
      .then(res => setHost(res.data))
      .catch(err => console.error(err));
  }, [id]);

  if (!host) return <div>Загрузка...</div>;

  return (
    <div>
      <h1>{host.name}</h1>
      <p><strong>ОС:</strong> {host.osPrettyName || host.osInfo}</p>
      <p><strong>Ядро:</strong> {host.kernelVersion}</p>
      <p><strong>Архитектура:</strong> {host.architecture}</p>
      <p><strong>IP-адрес:</strong> {host.ipAddress}</p>
      <p><strong>Последнее обновление данных:</strong> {new Date(host.lastUpdated).toLocaleString()}</p>

      <div style={{ marginTop: '1rem' }}>
        <button onClick={() => setActiveTab('packages')} style={{ marginRight: '0.5rem' }}>Установленные пакеты</button>
        <button onClick={() => setActiveTab('upgradable')}>Доступные обновления</button>
      </div>

      <div style={{ marginTop: '1rem' }}>
        {activeTab === 'packages' && (
          <div>
            <h3>Установленные пакеты (всего: {host.packages?.length})</h3>
            <input
              type="text"
              placeholder="Поиск по пакетам..."
              id="packageSearch"
              onChange={e => {
                const search = e.target.value.toLowerCase();
                const rows = document.querySelectorAll('.package-row');
                rows.forEach(row => {
                  const name = row.querySelector('.package-name').innerText.toLowerCase();
                  row.style.display = name.includes(search) ? '' : 'none';
                });
              }}
              style={{ marginBottom: '0.5rem', padding: '0.5rem' }}
            />
            <table border="1" cellPadding="5" style={{ width: '100%' }}>
              <thead>
                <tr><th>Название</th><th>Версия</th><th>Архитектура</th></tr>
              </thead>
              <tbody>
                {host.packages?.map(pkg => (
                  <tr key={pkg.name} className="package-row">
                    <td className="package-name">{pkg.name}</td>
                    <td>{pkg.version}</td>
                    <td>{pkg.architecture}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {activeTab === 'upgradable' && (
          <div>
            <h3>Доступные обновления (всего: {host.upgradablePackages?.length})</h3>
            <table border="1" cellPadding="5" style={{ width: '100%' }}>
              <thead>
                <tr><th>Название</th><th>Текущая версия</th><th>Доступная версия</th></tr>
              </thead>
              <tbody>
                {host.upgradablePackages?.map(pkg => (
                  <tr key={pkg.name}>
                    <td>{pkg.name}</td>
                    <td>{pkg.currentVersion}</td>
                    <td>{pkg.targetVersion}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default HostDetails;