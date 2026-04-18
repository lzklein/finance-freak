import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { searchAssets, getSteamPrice } from '../api';

const Search = () => {
  const [searchParams] = useSearchParams();
  const [results, setResults] = useState([]);
  const [prices, setPrices] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const query = searchParams.get('q');
  const navigate = useNavigate();

  useEffect(() => {
    if (!query) return;
    setPage(0);
    fetchResults(query, 0);
  }, [query]);

  useEffect(() => {
    if (!query) return;
    fetchResults(query, page);
  }, [page]);

  const fetchResults = async (q, p) => {
    setLoading(true);
    setError(null);
    setPrices({});
    try {
      const data = await searchAssets(q, p);
      console.log(data);
      setResults(data.results);
      setTotalPages(data.totalPages);
    //   fetchPrices(data.results);
    } catch (err) {
      setError('Failed to fetch results');
    } finally {
      setLoading(false);
    }
  }

const fetchPrices = async (assets) => {
    for (const asset of assets) {
      try {
        const price = await getSteamPrice(asset.name);
        setPrices(prev => ({ ...prev, [asset.name]: price }));
        await new Promise(resolve => setTimeout(resolve, 100));
      } catch (err) {
        // price unavailable for this item
      }
    }
  }

  const handleAdd = (asset) => {
    console.log('add to watchlist', asset);
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">
      <h1 className="text-2xl font-bold mb-2">Search Results</h1>
      {query && <p className="text-gray-400 mb-6">Showing results for "{query}"</p>}

      {loading && <p className="text-gray-400">Loading...</p>}
      {error && <p className="text-red-400">{error}</p>}

      {!loading && results.length === 0 && query && (
        <p className="text-gray-400">No results found for "{query}"</p>
      )}

      <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
        {results.map((asset) => {
          const price = prices[asset.name];
          return (
            <div key={asset.name} className="bg-gray-900 rounded-xl overflow-hidden hover:bg-gray-800 transition">
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
              </div>

              <div className="p-4">
                <p className="font-bold text-white text-sm mb-1 truncate">{asset.name}</p>

                {/* {price ? (
                  <p className="text-green-400 text-sm mb-3">
                    ${price.lowestAsk ? Number(price.lowestAsk).toFixed(2) : 'N/A'}
                  </p>
                ) : (
                  <p className="text-gray-500 text-sm mb-3">Loading price...</p>
                )} */}

                <div className="flex gap-2">
                  {asset.id && (
                    <button
                      onClick={() => navigate(`/asset/${asset.id}`)}
                      className="flex-1 bg-gray-700 hover:bg-gray-600 text-white text-sm py-1 rounded transition"
                    >
                      View
                    </button>
                  )}
                  <button
                    onClick={() => handleAdd(asset)}
                    className="flex-1 bg-green-600 hover:bg-green-700 text-white text-sm py-1 rounded transition"
                  >
                    + Add
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

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
    </div>
  )
}

export default Search