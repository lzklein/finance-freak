import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getWatchlistAssets, removeAssetFromWatchlist } from '../api'

const Asset = ({ watchlistId, onRemove }) => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!watchlistId) return;
    loadAssets();
  }, [watchlistId]);

  const loadAssets = async () => {
    setLoading(true);
    try {
      const data = await getWatchlistAssets(watchlistId);
      setAssets(data);
    } catch (err) {
      setError('Failed to load assets');
    } finally {
      setLoading(false);
    }
  }

  const handleRemove = async (assetId) => {
    if (!window.confirm('Delete this item? This cannot be undone.')) return;
    try {
      await removeAssetFromWatchlist(watchlistId, assetId);
      setAssets(prev => prev.filter(a => a.assetId !== assetId));
      onRemove && onRemove();
    } catch (err) {
      setError('Failed to remove asset');
    }
  }

  const trimExterior = (name) => name.replace(/\s*\([^)]*\)\s*$/, '').trim();

  if (loading) return (
    <div className="flex items-center gap-2 text-gray-400 py-8">
      <div className="w-4 h-4 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
      Loading assets...
    </div>
  );

  if (error) return <p className="text-red-400 text-sm">{error}</p>;

  if (assets.length === 0) return (
    <div className="text-center py-16 text-gray-500">
      <p className="text-lg mb-2">No assets in this watchlist</p>
      <p className="text-sm">Search for CS2 skins and add them here</p>
    </div>
  );

  return (
    <div className="flex flex-col gap-3">
      {assets.map((item) => {
        const price = item.prices?.STEAM;
        const exterior = item.name?.match(/\(([^)]+)\)/)?.[1];
        const isStatTrak = item.name?.toLowerCase().includes('stattrak');
        const isSouvenir = item.name?.toLowerCase().includes('souvenir');

        console.log(item);

        return (
          <div
            key={item.id}
            className="bg-gray-900 rounded-xl p-4 flex items-center gap-4 hover:bg-gray-800 transition cursor-pointer"
            onClick={() => navigate(`/asset/${item.id}`)}
          >
            {/* image */}
            {item.imageUrl ? (
              <img
                src={item.imageUrl}
                alt={item.name}
                className="w-16 h-16 object-contain bg-gray-800 rounded p-1 flex-shrink-0"
              />
            ) : (
              <div className="w-16 h-16 bg-gray-800 rounded flex-shrink-0" />
            )}

            {/* info */}
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <p className="font-bold text-white text-sm truncate">
                  {trimExterior(item.name)}
                </p>
                {isStatTrak && (
                  <span className="text-xs px-1.5 py-0.5 rounded bg-yellow-600 text-black font-bold flex-shrink-0">
                    ST
                  </span>
                )}
                {isSouvenir && (
                  <span className="text-xs px-1.5 py-0.5 rounded bg-yellow-400 text-black font-bold flex-shrink-0">
                    SV
                  </span>
                )}
              </div>
              {exterior && (
                <p className="text-gray-500 text-xs mt-0.5">{exterior}</p>
              )}
            </div>

            {/* price */}
            <div className="text-right flex-shrink-0">
              {price?.lowestAsk ? (
                <>
                  <p className="text-green-400 font-bold text-sm">
                    ${Number(price.lowestAsk).toFixed(2)}
                  </p>
                  {price.lastSale && (
                    <p className="text-gray-500 text-xs">
                      Last: ${Number(price.lastSale).toFixed(2)}
                    </p>
                  )}
                </>
              ) : (
                <p className="text-gray-500 text-sm">No price</p>
              )}
            </div>

            {/* remove button */}
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleRemove(item.assetId);
              }}
              className="text-gray-600 hover:text-red-400 transition text-lg flex-shrink-0 ml-2"
            >
              ✕
            </button>
          </div>
        );
      })}
    </div>
  )
}

export default Asset