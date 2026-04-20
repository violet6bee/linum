import { useState } from 'react';
import axios from 'axios';

function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post('/api/auth/login', { username, password });
      localStorage.setItem('adminToken', res.data.token);
      onLogin();
    } catch (err) {
      setError('Неверный логин или пароль');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Вход для администратора</h2>
      <input placeholder="Логин" value={username} onChange={e => setUsername(e.target.value)} />
      <input type="password" placeholder="Пароль" value={password} onChange={e => setPassword(e.target.value)} />
      <button type="submit">Войти</button>
      {error && <p style={{color:'red'}}>{error}</p>}
    </form>
  );
}

export default Login;