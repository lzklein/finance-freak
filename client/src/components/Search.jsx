import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'

const Search = () => {
  const [searchParams] = useSearchParams();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const query = searchParams.get('q');
  const navigate = useNavigate();

  useEffect(() => {
    if (!query) return;
    fetchResults(query);
  }, [query]);

  const fetchResults = async (q) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`http://localhost:8080/api/assets/search/alpaca?query=${q}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      setResults(data);
    } catch (err) {
      setError('Failed to fetch results');
    } finally {
      setLoading(false);
    }
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

      <div className="flex flex-col gap-3">
        {results.map((asset) => (
          <div
            key={asset.symbol}
            className="bg-gray-900 rounded-xl p-4 flex items-center justify-between hover:bg-gray-800 transition cursor-pointer"
            onClick={() => navigate(`/asset/${asset.symbol}`)}
          >
            <div>
              <p className="font-bold text-white">{asset.symbol}</p>
              <p className="text-gray-400 text-sm">{asset.name}</p>
            </div>
            <div className="flex items-center gap-4">
              <span className={`text-xs px-2 py-1 rounded ${
                asset.assetType === 'CRYPTO' 
                  ? 'bg-yellow-500 text-black' 
                  : 'bg-green-700 text-white'
              }`}>
                {asset.assetType}
              </span>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  // add to watchlist — will wire up later
                  console.log('add to watchlist', asset);
                }}
                className="bg-green-600 hover:bg-green-700 text-white text-sm px-3 py-1 rounded transition"
              >
                + Add
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Search