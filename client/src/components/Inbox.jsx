import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAlertHistory } from '../api'

const Inbox = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const data = await getAlertHistory();
      setHistory(data);
    } catch (err) {
      setError('Failed to load alert history');
    } finally {
      setLoading(false);
    }
  }

  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">
      <h1 className="text-2xl font-bold mb-2">Alert Inbox</h1>
      <p className="text-gray-400 mb-6">History of your triggered price alerts</p>

      {loading && (
        <div className="flex items-center gap-2 text-gray-400">
          <div className="w-4 h-4 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
          Loading...
        </div>
      )}

      {error && <p className="text-red-400">{error}</p>}

      {!loading && history.length === 0 && (
        <div className="text-center py-24 text-gray-500">
          <p className="text-5xl mb-4">🔔</p>
          <p className="text-lg mb-2">No alerts triggered yet</p>
          <p className="text-sm">Set price alerts on any skin and they'll appear here when triggered</p>
          <button
            onClick={() => navigate('/dashboard')}
            className="mt-6 bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded transition text-sm"
          >
            Go to Dashboard
          </button>
        </div>
      )}

      <div className="flex flex-col gap-3 max-w-3xl">
        {history.map((item) => (
          <div
            key={item.id}
            className="bg-gray-900 rounded-xl p-4 flex items-center gap-4 hover:bg-gray-800 transition cursor-pointer"
            onClick={() => item.alertId && navigate(`/asset/${item.alertId}`)}
          >
            {/* icon */}
            <div className="w-10 h-10 rounded-full bg-green-900 flex items-center justify-center flex-shrink-0">
              <span className="text-green-400 text-lg">🔔</span>
            </div>

            {/* info */}
            <div className="flex-1 min-w-0">
              <p className="text-white font-semibold text-sm truncate">
                {item.assetName}
              </p>
              <p className="text-gray-400 text-xs mt-0.5">
                Price alert triggered
              </p>
            </div>

            {/* price + time */}
            <div className="text-right flex-shrink-0">
              <p className="text-green-400 font-bold text-sm">
                ${Number(item.triggeredPrice).toFixed(2)}
              </p>
              <p className="text-gray-500 text-xs mt-0.5">
                {formatDate(item.triggeredAt)}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Inbox