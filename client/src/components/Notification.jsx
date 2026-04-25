import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAlertHistory } from '../api'

const Notification = ({ onClose }) => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadRecent();
  }, []);

  const loadRecent = async () => {
    try {
      const data = await getAlertHistory();
      setHistory(data.slice(0, 5));
    } catch (err) {
      console.error('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  }

  const formatTime = (dateStr) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    return `${diffDays}d ago`;
  }

  const handleViewAll = () => {
    onClose();
    navigate('/inbox');
  }

  const handleItemClick = (item) => {
    onClose();
    if (item.assetId) navigate(`/asset/${item.assetId}`);
  }

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-xl shadow-2xl w-80 overflow-hidden">
      {/* header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-700">
        <h3 className="text-white font-bold text-sm">Notifications</h3>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-white transition text-sm"
        >
          ✕
        </button>
      </div>

      {/* content */}
      {loading ? (
        <div className="flex items-center justify-center py-8">
          <div className="w-4 h-4 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
        </div>
      ) : history.length === 0 ? (
        <div className="text-center py-8 px-4">
          <p className="text-gray-500 text-sm">No notifications yet</p>
          <p className="text-gray-600 text-xs mt-1">
            Triggered alerts will appear here
          </p>
        </div>
      ) : (
        <div className="flex flex-col">
          {history.map((item) => (
            <div
              key={item.id}
              onClick={() => handleItemClick(item)}
              className="flex items-center gap-3 px-4 py-3 hover:bg-gray-800 transition cursor-pointer border-b border-gray-800 last:border-0"
            >
              <div className="w-8 h-8 rounded-full bg-green-900 flex items-center justify-center flex-shrink-0">
                <span className="text-green-400 text-xs">🔔</span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-white text-xs font-semibold truncate">
                  {item.assetName}
                </p>
                <p className="text-gray-400 text-xs">
                  Triggered at ${Number(item.triggeredPrice).toFixed(2)}
                </p>
              </div>
              <span className="text-gray-500 text-xs flex-shrink-0">
                {formatTime(item.triggeredAt)}
              </span>
            </div>
          ))}
        </div>
      )}

      {/* footer */}
      <div className="border-t border-gray-700 px-4 py-3">
        <button
          onClick={handleViewAll}
          className="w-full text-center text-green-400 hover:text-green-300 text-xs transition"
        >
          View all in Inbox →
        </button>
      </div>
    </div>
  )
}

export default Notification