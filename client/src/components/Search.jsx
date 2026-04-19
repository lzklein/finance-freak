import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { searchAssets, getSteamPrice } from '../api';
import WatchlistModal from './WatchlistModal'

const Search = () => {
  const [searchParams] = useSearchParams();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [expandedCard, setExpandedCard] = useState(null);
  const [cardPrices, setCardPrices] = useState({});
  const [priceLoading, setPriceLoading] = useState(false);
  const query = searchParams.get('q');
  const navigate = useNavigate();
  const [modalAsset, setModalAsset] = useState(null);

  const trimExterior = (name) => {
  return name.replace(/\s*\([^)]*\)\s*$/, '').trim();
}

  useEffect(() => {
    if (!query) return;
    setPage(0);
    setExpandedCard(null);
  }, [query]);

  useEffect(() => {
    if (!query) return;
    fetchResults(query, page);
  }, [query, page]);

  const fetchResults = async (q, p) => {
    setLoading(true);
    setError(null);
    setExpandedCard(null);
    setCardPrices({});
    try {
      const data = await searchAssets(q, p);
      setResults(data.results);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError('Failed to fetch results');
    } finally {
      setLoading(false);
    }
  }

  
  const handleCardClick = async (asset) => {
    if (expandedCard === asset.name) {
      setExpandedCard(null);
      return;
    }

    setExpandedCard(asset.name);

    if (!cardPrices[asset.name]) {
      setPriceLoading(true);
      try {
        const price = await getSteamPrice(asset.name);
        setCardPrices(prev => ({ ...prev, [asset.name]: price }));
      } catch (err) {
        setCardPrices(prev => ({ ...prev, [asset.name]: null }));
      } finally {
        setPriceLoading(false);
      }
    }
  }

  const handleAdd = (e, asset) => {
    e.stopPropagation();
    setModalAsset(asset);
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">
      <h1 className="text-2xl font-bold mb-2">Search Results</h1>
      {query && <p className="text-gray-400 mb-6">Showing results for "{query}"</p>}

      {loading && (
        <div className="flex items-center gap-2 text-gray-400">
          <div className="w-4 h-4 border-2 border-green-500 border-t-transparent rounded-full animate-spin" />
          Searching...
        </div>
      )}
      {error && <p className="text-red-400">{error}</p>}

      {!loading && results.length === 0 && query && (
        <p className="text-gray-400">No results found for "{query}"</p>
      )}

      <div className="grid grid-cols-2 md:grid-cols-3 gap-4 items-start">
        {results.map((asset) => {
          const isExpanded = expandedCard === asset.name;
          const price = cardPrices[asset.name];
          const exterior = asset.name.match(/\(([^)]+)\)/)?.[1];
          const isStatTrak = asset.name.toLowerCase().includes('stattrak');
          const isSouvenir = asset.name.toLowerCase().includes('souvenir');
          return (
            <div
              key={asset.name}
              onClick={() => handleCardClick(asset)}
              className={`bg-gray-900 rounded-xl overflow-hidden cursor-pointer border-2 transition-colors duration-200 ${
                isExpanded
                  ? 'border-green-500'
                  : 'border-transparent hover:border-gray-700'
              }`}
            >
              {/* image */}
              <div className="relative">
                {asset.imageUrl ? (
                  <img
                    src={asset.imageUrl}
                    alt={asset.name}
                    className="w-full h-48 object-contain bg-gray-800 p-4"
                  />
                ) : (
                  <div className="w-full h-48 bg-gray-800 flex items-center justify-center">
                    <p className="text-gray-500 text-sm">No image</p>
                  </div>
                )}
                <span className="absolute top-2 right-2 text-xs px-2 py-1 rounded bg-green-700 text-white">
                  CS2
                </span>
                {isStatTrak && (
                  <span className="absolute top-2 left-2 text-xs px-2 py-1 rounded bg-yellow-600 text-black font-bold">
                    ST
                  </span>
                )}
                {isSouvenir && (
                  <span className="absolute top-2 left-2 text-xs px-2 py-1 rounded bg-yellow-400 text-black font-bold">
                    SV
                  </span>
                )}
              </div>

              {/* card body */}
              <div className="p-4">
                <p className="font-bold text-white text-sm truncate">{trimExterior(asset.name)}</p>
                {exterior && (
                  <p className="text-gray-500 text-xs mt-0.5">{exterior}</p>
                )}

                {/* expandable section */}
                <div
                  style={{
                    maxHeight: isExpanded ? '400px' : '0px',
                    overflow: 'hidden',
                    transition: 'max-height 0.35s ease-in-out, opacity 0.3s ease-in-out',
                    opacity: isExpanded ? 1 : 0
                  }}
                >
                  <div className="mt-3 border-t border-gray-700 pt-3 flex flex-col gap-2">

                    {/* skin metadata */}
                    {asset.weaponType && (
                      <div className="flex justify-between text-xs">
                        <span className="text-gray-400">Type</span>
                        <span className="text-gray-300">{asset.weaponType}</span>
                      </div>
                    )}
                    {isStatTrak && (
                      <div className="flex justify-between text-xs">
                        <span className="text-gray-400">StatTrak™</span>
                        <span className="text-yellow-400">✓</span>
                      </div>
                    )}
                    {isSouvenir && (
                      <div className="flex justify-between text-xs">
                        <span className="text-gray-400">Souvenir</span>
                        <span className="text-yellow-400">✓</span>
                      </div>
                    )}
                    {/* price */}
                    {priceLoading && !price ? (
                      <p className="text-gray-400 text-sm animate-pulse">Fetching price...</p>
                    ) : price?.lowestAsk ? (
                      <>
                        <div className="flex justify-between text-sm">
                          <span className="text-gray-400">Lowest Ask</span>
                          <span className="text-green-400 font-bold">
                            ${Number(price.lowestAsk).toFixed(2)}
                          </span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-gray-400">Last Sale</span>
                          <span className="text-white">
                            ${Number(price.lastSale).toFixed(2)}
                          </span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-gray-400">Volume (24h)</span>
                          <span className="text-yellow-400">{price.volume}</span>
                        </div>
                      </>
                    ) : (
                      <p className="text-gray-500 text-sm">Price unavailable</p>
                    )}

                    {/* actions */}
                    <div className="flex gap-2 mt-1">
                      {asset.id && (
                        <button
                          onClick={(e) => { e.stopPropagation(); navigate(`/asset/${asset.id}`); }}
                          className="flex-1 bg-gray-700 hover:bg-gray-600 text-white text-sm py-1.5 rounded transition"
                        >
                          View
                        </button>
                      )}
                      <button
                        onClick={(e) => handleAdd(e, asset)}
                        className="flex-1 bg-green-600 hover:bg-green-700 text-white text-sm py-1.5 rounded transition"
                      >
                        + Add
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-3 mt-8">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-4 py-2 bg-gray-800 rounded disabled:opacity-40 hover:bg-gray-700 transition"
          >
            Previous
          </button>
          <span className="px-4 py-2 text-gray-400">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 bg-gray-800 rounded disabled:opacity-40 hover:bg-gray-700 transition"
          >
            Next
          </button>
        </div>
      )}
    {modalAsset && (
    <WatchlistModal
        asset={modalAsset}
        onClose={() => setModalAsset(null)}
        onSuccess={() => setModalAsset(null)}
    />
    )}
    </div>
  )
}

export default Search