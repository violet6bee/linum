import { useState, useEffect } from 'react';
import api from '../api';

function AgentsManagement() {
  const [agents, setAgents] = useState([]);
  const [newName, setNewName] = useState('');
  const [message, setMessage] = useState('');

  const fetchAgents = async () => {
    try {
      const res = await api.get('/admin/agents');
      setAgents(res.data);
    } catch (err) { console.error(err); }
  };

  useEffect(() => { fetchAgents(); }, []);

  const createAgent = async () => {
    if (!newName) return;
    try {
      const res = await api.post('/admin/agents', { name: newName });
      setMessage(`Токен: ${res.data.token}. Скопируйте его в конфиг агента.`);
      setNewName('');
      fetchAgents();
    } catch (err) { setMessage('Ошибка создания'); }
  };

  const deleteAgent = async (id) => {
    if (!confirm('Удалить агента?')) return;
    await api.delete(`/admin/agents/${id}`);
    fetchAgents();
  };

  return (
    <div>
      <h2>Управление агентами</h2>
      <div>
        <input value={newName} onChange={e => setNewName(e.target.value)} placeholder="Имя хоста" />
        <button onClick={createAgent}>Создать агента</button>
      </div>
      {message && <pre>{message}</pre>}
      <table border="1">
        <thead><tr><th>ID</th><th>Имя</th><th>Токен</th><th>Последнее обновление</th></tr></thead>
        <tbody>
          {agents.map(agent => (
            <tr key={agent.id}>
              <td>{agent.id}</td>
              <td>{agent.name}</td>
              <td>{agent.token}</td>
              <td>{agent.lastUpdated ? new Date(agent.lastUpdated).toLocaleString() : 'никогда'}</td>
              <td><button onClick={() => deleteAgent(agent.id)}>Удалить</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AgentsManagement;