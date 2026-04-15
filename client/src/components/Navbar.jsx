import React, { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import Notification from './Notification'

const Navbar = () => {
  const isLoggedIn = !!localStorage.getItem('token');
  const navigate = useNavigate();
  const [showNotifications, setShowNotifications] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/');
  }

  const handleSearch = (e) => {
    e.preventDefault();
    const query = e.target.search.value;
    if (query.trim()) {
      navigate(`/search?q=${query}`);
    }
  }

  const handleLogin = () => {
    localStorage.setItem('token', 'fake-token');
    navigate('/dashboard');
  }

  return (
    <nav className="flex items-center justify-between p-4 bg-gray-900 text-white">
      
      <NavLink to="/" className="text-xl font-bold">FinanceTracker</NavLink>

      {isLoggedIn && (
        <form onSubmit={handleSearch} className="flex-1 mx-8">
          <input
            name="search"
            type="text"
            placeholder="Search stocks or crypto..."
            className="w-full px-4 py-2 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500"
          />
        </form>
      )}

      <div className="flex items-center gap-6">
        {isLoggedIn ? (
          <>
            <div className="relative">
              <button 
                onClick={() => setShowNotifications(!showNotifications)} 
                className="hover:text-green-400 text-xl">
                🔔
              </button>
              {showNotifications && (
                <div className="absolute right-0 top-8 z-50">
                  <Notification onClose={() => setShowNotifications(false)} />
                </div>
              )}
            </div>
            <NavLink to="/dashboard" className="hover:text-green-400">Dashboard</NavLink>
            <button onClick={handleLogout} className="hover:text-red-400">Logout</button>
          </>
        ) : (
          <>
            <button onClick={handleLogin}  className="hover:text-green-400">Login</button>
            <NavLink to="/register" className="hover:text-green-400">Register</NavLink>
          </>
        )}
      </div>

    </nav>
  )
}

export default Navbar