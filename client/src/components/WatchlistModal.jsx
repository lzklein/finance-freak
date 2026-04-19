import React, { useState, useEffect } from 'react'
import { getWatchlists, createWatchlist, saveAssetAndAdd } from '../api'

const WatchlistModal = ({ asset, onClose, onSuccess }) => {
  const [watchlists, setWatchlists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newWatchlistName, setNewWatchlistName] = useState('');
  const [creating, setCreating] = useState(false);
  const [adding, setAdding] = useState(null);
  const [error, setError] = useState(null);
  const [added, setAdded] = useState([]);

  useEffect(() => {
    loadWatchlists();
  }, []);

  const loadWatchlists = async () => {
    try {
      const data = await getWatchlists();
      setWatchlists(data);
    } catch (err) {
      setError('Failed to load watchlists');
    } finally {
      setLoading(false);
    }
  }

  const handleCreate = async () => {
    if (!newWatchlistName.trim()) return;
    setCreating(true);
    try {
      const newList = await createWatchlist({ name: newWatchlistName.trim() });
      setWatchlists(prev => [...prev, newList]);
      setNewWatchlistName('');
    } catch (err) {
      setError('Failed to create watchlist');
    } finally {
      setCreating(false);
    }
  }

  const handleAdd = async (watchlistId) => {
    setAdding(watchlistId);
    setError(null);
    try {
      await saveAssetAndAdd(asset, watchlistId);
      setAdded(prev => [...prev, watchlistId]);
      onSuccess && onSuccess();
    } catch (err) {
      setError('Failed to add to watchlist');
    } finally {
      setAdding(null);
    }
  }

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-gray-900 rounded-xl p-6 w-full max-w-md shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-bold text-white">Add to Watchlist</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white text-xl">✕</button>
        </div>

        <p className="text-gray-400 text-sm mb-4 truncate">
          {asset.name}
        </p>

        {error && <p className="text-red-400 text-sm mb-3">{error}</p>}

        {loading ? (
          <p className="text-gray-400 text-sm animate-pulse">Loading watchlists...</p>
        ) : (
          <div className="flex flex-col gap-2 mb-4">
            {watchlists.length === 0 && (
              <p className="text-gray-500 text-sm">No watchlists yet — create one below.</p>
            )}
            {watchlists.map(wl => {
              const isAdded = added.includes(wl.id);
              const isAdding = adding === wl.id;
              return (
                <div
                  key={wl.id}
                  className="flex items-center justify-between bg-gray-800 rounded-lg px-4 py-3"
                >
                  <span className="text-white text-sm">{wl.name}</span>
                  <button
                    onClick={() => !isAdded && handleAdd(wl.id)}
                    disabled={isAdded || isAdding}
                    className={`text-sm px-3 py-1 rounded transition ${
                      isAdded
                        ? 'bg-gray-600 text-gray-400 cursor-default'
                        : 'bg-green-600 hover:bg-green-700 text-white'
                    }`}
                  >
                    {isAdded ? '✓ Added' : isAdding ? 'Adding...' : '+ Add'}
                  </button>
                </div>
              );
            })}
          </div>
        )}

        {/* create new watchlist */}
        <div className="border-t border-gray-700 pt-4">
          <p className="text-gray-400 text-xs mb-2">Create new watchlist</p>
          <div className="flex gap-2">
            <input
              type="text"
              value={newWatchlistName}
              onChange={e => setNewWatchlistName(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleCreate()}
              placeholder="Watchlist name..."
              className="flex-1 px-3 py-2 rounded bg-gray-700 text-white text-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <button
              onClick={handleCreate}
              disabled={creating || !newWatchlistName.trim()}
              className="bg-green-600 hover:bg-green-700 disabled:opacity-40 text-white text-sm px-3 py-2 rounded transition"
            >
              {creating ? '...' : 'Create'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default WatchlistModal