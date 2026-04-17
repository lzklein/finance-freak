import React, { useState } from 'react'
import Popular from './Popular';
import Top from './Top';
import Watchlist from './Watchlist';

// main dashboard
// contains watchlist, popular top

// if not logged in no watchlist no popular
// top and other stuff decide on later to view, no interaction if not logged in
const Dashboard = () => {
  const [data, setData] = useState(null);

  const testFetch = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/assets/test-alpaca', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const json = await response.json();
      console.log(json);
      setData(json);
    } catch (err) {
      console.error(err);
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">
      <h1 className="text-2xl font-bold mb-4">Dashboard</h1>
      <button 
        onClick={testFetch}
        className="bg-green-600 hover:bg-green-700 px-4 py-2 rounded">
        Test Alpaca Fetch
      </button>
      {data && <pre className="mt-4 text-xs text-gray-400">{JSON.stringify(data, null, 2)}</pre>}
    </div>
  )
}

export default Dashboard