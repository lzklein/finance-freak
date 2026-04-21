import React, { useState, useEffect } from 'react'
import { getPopularAssets } from '../api'

const Popular = () => {
  const [assets, setAssets] = useState([]);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await getPopularAssets();
        setAssets(data);
      } catch (err) {
        console.error('Failed to load popular assets');
      }
    }
    load();
  }, []);

  const trimExterior = (name) => name.replace(/\s*\([^)]*\)\s*$/, '').trim();

  if (assets.length === 0) return null;

  return (
    <div className="bg-gray-900 border-b border-gray-800 overflow-hidden">
      <h3>Popular Skins</h3>
      <div className="flex animate-marquee whitespace-nowrap py-3">
        {[...assets, ...assets].map((asset, i) => {
          const price = asset.prices?.STEAM;
          return (
            <div key={i} className="flex items-center gap-3 px-6 border-r border-gray-700">
              {asset.imageUrl && (
                <img
                  src={asset.imageUrl}
                  alt={asset.name}
                  className="w-8 h-8 object-contain"
                />
              )}
              <div>
                <p className="text-white text-xs font-semibold">
                  {trimExterior(asset.name)}
                </p>
                {price?.lowestAsk ? (
                  <p className="text-green-400 text-xs">
                    ${Number(price.lowestAsk).toFixed(2)}
                  </p>
                ) : (
                  <p className="text-gray-500 text-xs">—</p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  )
}

export default Popular