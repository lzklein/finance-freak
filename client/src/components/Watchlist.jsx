import React, { useState, useEffect } from 'react'
import { getWatchlists, deleteWatchlist, createWatchlist } from '../api'
import Asset from './Asset'

const Watchlist = () => {
  const [watchlists, setWatchlists] = useState([]);
  const [activeTab, setActiveTab] = useState(null);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState('');
  const [showNewInput, setShowNewInput] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadWatchlists();
  }, []);

  const loadWatchlists = async () => {
    try {
      const data = await getWatchlists();
      setWatchlists(data);
      if (data.length > 0) setActiveTab(data[0].id);
    } catch (err) {
      setError('Failed to load watchlists');
    } finally {
      setLoading(false);
    }
  }

  const handleCreate = async () => {
    if (!newName.trim()) return;
    setCreating(true);
    try {
      const created = await createWatchlist({ name: newName.trim() });
      setWatchlists(prev => [...prev, created]);
      setActiveTab(created.id);
      setNewName('');
      setShowNewInput(false);
    } catch (err) {
      setError('Failed to create watchlist');
    } finally {
      setCreating(false);
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this watchlist? This cannot be undone.')) return;
    try {
      await deleteWatchlist(id);
      const updated = watchlists.filter(w => w.id !== id);
      setWatchlists(updated);
      if (activeTab === id) {
        setActiveTab(updated.length > 0 ? updated[0].id : null);
      }
    } catch (err) {
      setError('Failed to delete watchlist');
    }
  }

  const activeWatchlist = watchlists.find(w => w.id === activeTab);

  if (loading) return (
    <div className="flex items-center gap-2 text-gray-400">
      <div className="w-4 h-4 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
      Loading watchlists...
    </div>
  );

  return (
    <div>
      {error && <p className="text-red-400 text-sm mb-3">{error}</p>}

      {/* tabs */}
      <div className="flex items-center gap-2 mb-4 flex-wrap">
        {watchlists.map(wl => (
          <div key={wl.id} className="flex items-center">
            <button
              onClick={() => setActiveTab(wl.id)}
              className={`px-4 py-2 rounded-l text-sm font-medium transition ${
                activeTab === wl.id
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-800 text-gray-400 hover:text-white'
              }`}
            >
              {wl.name}
            </button>
            <button
              onClick={() => handleDelete(wl.id)}
              className={`px-2 py-2 rounded-r text-xs transition ${
                activeTab === wl.id
                  ? 'bg-green-700 text-white hover:bg-red-600'
                  : 'bg-gray-700 text-gray-500 hover:bg-red-600 hover:text-white'
              }`}
            >
              ✕
            </button>
          </div>
        ))}

        {/* new watchlist */}
        {showNewInput ? (
          <div className="flex gap-2">
            <input
              type="text"
              value={newName}
              onChange={e => setNewName(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleCreate()}
              placeholder="Watchlist name..."
              autoFocus
              className="px-3 py-2 rounded bg-gray-800 text-white text-sm placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <button
              onClick={handleCreate}
              disabled={creating || !newName.trim()}
              className="bg-green-600 hover:bg-green-700 disabled:opacity-40 text-white text-sm px-3 py-2 rounded transition"
            >
              {creating ? '...' : 'Create'}
            </button>
            <button
              onClick={() => { setShowNewInput(false); setNewName(''); }}
              className="bg-gray-700 hover:bg-gray-600 text-white text-sm px-3 py-2 rounded transition"
            >
              Cancel
            </button>
          </div>
        ) : (
          <button
            onClick={() => setShowNewInput(true)}
            className="px-4 py-2 rounded bg-gray-800 text-gray-400 hover:text-white text-sm transition border border-dashed border-gray-700 hover:border-gray-500"
          >
            + New
          </button>
        )}
      </div>

      {/* active watchlist content */}
      {watchlists.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          <p className="text-lg mb-2">No watchlists yet</p>
          <p className="text-sm">Create one above to start tracking skins</p>
        </div>
      ) : activeWatchlist ? (
        <Asset watchlistId={activeTab} onRemove={loadWatchlists} />
      ) : null}
    </div>
  )
}

export default Watchlist