import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Popular from './Popular'

const Home = () => {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem('token');

  useEffect(() => {
    if (isLoggedIn) {
      navigate('/dashboard');
    }
  }, []);

  return (
    <div className="min-h-screen bg-gray-950 text-white">

      <Popular />

      {/* hero */}
      <div className="flex flex-col items-center justify-center text-center px-8 py-24">
        <h1 className="text-5xl font-bold mb-4">
          Track CS2 Skin Prices
        </h1>
        <p className="text-gray-400 text-lg max-w-xl mb-8">
          Monitor your CS2 skin portfolio, set price alerts, and never miss a market move.
          Real-time Steam Market data, all in one place.
        </p>
        <div className="flex gap-4">
          <button
            onClick={() => navigate('/register')}
            className="bg-green-600 hover:bg-green-700 text-white font-semibold px-8 py-3 rounded-lg transition text-lg"
          >
            Get Started
          </button>
          <button
            onClick={() => navigate('/login')}
            className="bg-gray-800 hover:bg-gray-700 text-white font-semibold px-8 py-3 rounded-lg transition text-lg"
          >
            Login
          </button>
        </div>
      </div>

      {/* feature highlights */}
      <div className="grid grid-cols-3 gap-6 px-16 pb-24">
        <div className="bg-gray-900 rounded-xl p-6 text-center">
          <p className="text-4xl mb-4">📈</p>
          <h3 className="text-white font-bold mb-2">Price Tracking</h3>
          <p className="text-gray-400 text-sm">
            Live Steam Market prices updated every minute across your entire watchlist
          </p>
        </div>
        <div className="bg-gray-900 rounded-xl p-6 text-center">
          <p className="text-4xl mb-4">🔔</p>
          <h3 className="text-white font-bold mb-2">Price Alerts</h3>
          <p className="text-gray-400 text-sm">
            Set custom price thresholds and get notified the moment a skin hits your target
          </p>
        </div>
        <div className="bg-gray-900 rounded-xl p-6 text-center">
          <p className="text-4xl mb-4">📊</p>
          <h3 className="text-white font-bold mb-2">Portfolio Insights</h3>
          <p className="text-gray-400 text-sm">
            Track your best performers and see how your watchlist has moved since you added them
          </p>
        </div>
      </div>

    </div>
  )
}

export default Home