// imports
import './App.css';
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// components
import Header from './components/Header';
import Footer from './components/Footer';
import Home from './components/Home';
import Inbox from './components/Inbox';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Confirmation from './components/Confirmation';
import Login from './components/Login';
import Search from './components/Search';
import AssetPage from './components/AssetPage'

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Header/>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/search" element={<Search />} />
            <Route path="/register" element={<Register />} />
            <Route path="/confirmation" element={<Confirmation />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/inbox" element={<Inbox />} />
            <Route path="/asset/:id" element={<AssetPage />} />
            <Route path="/" element={<Home />} />
          </Routes>
        <Footer/>
      </div>
    </BrowserRouter>

    
  );
}

export default App;
