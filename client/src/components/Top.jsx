import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getTopPerformers } from '../api'

const Top = () => {
  const [performers, setPerformers] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadPerformers();
  }, []);

  const loadPerformers = async () => {
    try {
      const data = await getTopPerformers();
      setPerformers(data);
    } catch (err) {
      console.error('Failed to load top performers');
    } finally {
      setLoading(false);
    }
  }

  const trimExterior = (name) => name.replace(/\s*\([^)]*\)\s*$/, '').trim();

  return (
    <div className="bg-gray-900 rounded-xl p-4">
      <h2 className="text-white font-bold text-sm mb-4 uppercase tracking-wider">
        Top Performers
      </h2>

      {loading && (
        <div className="flex items-center gap-2 text-gray-400 py-4">
          <div className="w-4 h-4 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}

      {!loading && performers.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <p className="text-sm">No data yet</p>
          <p className="text-xs mt-1">Add skins to your watchlist to track performance</p>
        </div>
      )}

      <div className="flex flex-col gap-2">
        {performers.map((asset, i) => {
          const price = asset.prices?.STEAM;
          const changePct = asset.changePct;
          const isPositive = changePct > 0;
          const isNegative = changePct < 0;

          return (
            <div
              key={asset.id}
              onClick={() => navigate(`/asset/${asset.id}`)}
              className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-800 transition cursor-pointer"
            >
              {/* rank */}
              <span className="text-gray-600 text-xs font-bold w-4 flex-shrink-0">
                {i + 1}
              </span>

              {/* image */}
              {asset.imageUrl ? (
                <img
                  src={asset.imageUrl}
                  alt={asset.name}
                  className="w-10 h-10 object-contain bg-gray-800 rounded p-1 flex-shrink-0"
                />
              ) : (
                <div className="w-10 h-10 bg-gray-800 rounded flex-shrink-0" />
              )}

              {/* info */}
              <div className="flex-1 min-w-0">
                <p className="text-white text-xs font-semibold truncate">
                  {trimExterior(asset.name)}
                </p>
                {price?.lowestAsk && (
                  <p className="text-gray-400 text-xs">
                    ${Number(price.lowestAsk).toFixed(2)}
                  </p>
                )}
              </div>

              {/* change */}
              {changePct !== null && changePct !== undefined && (
                <span className={`text-xs font-bold flex-shrink-0 ${
                  isPositive ? 'text-green-400' :
                  isNegative ? 'text-red-400' :
                  'text-gray-400'
                }`}>
                  {isPositive ? '+' : ''}{Number(changePct).toFixed(1)}%
                </span>
              )}
            </div>
          );
        })}
      </div>
    </div>
  )
}

export default Top