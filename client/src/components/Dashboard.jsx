import React from 'react'
import Popular from './Popular';
import Top from './Top';
import Watchlist from './Watchlist';

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