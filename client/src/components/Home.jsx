import React from 'react'
import Dashboard from './Dashboard';

const Home = () => {
  return (
    <div>
      <button onClick={()=>{console.log(localStorage.getItem('token'))}}>test</button>
      <Dashboard/>
    </div>
  )
}

export default Home