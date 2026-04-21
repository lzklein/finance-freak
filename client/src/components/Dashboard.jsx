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
    <div className="min-h-screen bg-gray-950 text-white">
      <Popular />
      <div className="flex gap-6 p-6">
        <div className="flex-1">
          <Watchlist />
        </div>
        <div className="w-72 flex-shrink-0">
          <Top />
        </div>
      </div>
    </div>
  )
}

export default Dashboard