import React from 'react'
import Popular from './Popular';
import Top from './Top';
import Watchlist from './Watchlist';

// main dashboard
// contains watchlist, popular top

// if not logged in no watchlist no popular
// top and other stuff decide on later to view, no interaction if not logged in
const Dashboard = () => {
  return (
    <div>
      <Popular/>
      <Top/>
      <Watchlist/>
    </div>
  )
}

export default Dashboard