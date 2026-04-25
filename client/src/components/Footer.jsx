import React from 'react'
import { useNavigate } from 'react-router-dom'

const Footer = () => {
  const navigate = useNavigate();

  return (
    <footer className="bg-gray-900 border-t border-gray-800 text-gray-400 py-8 px-16 mt-auto">
      <div className="flex justify-between items-center">

        <div>
          <p className="text-white font-bold text-lg mb-1">FinanceFreak</p>
          <p className="text-xs">CS2 skin price tracking and alerts</p>
        </div>

        <div className="flex gap-8 text-sm">
          <button onClick={() => navigate('/')} className="hover:text-white transition">
            Home
          </button>
          <button onClick={() => navigate('/dashboard')} className="hover:text-white transition">
            Dashboard
          </button>
          <button onClick={() => navigate('/inbox')} className="hover:text-white transition">
            Inbox
          </button>
        </div>

        <div className="text-xs text-right">
          <p>Price data via Steam Market API</p>
          <p className="mt-1">© 2026 FinanceFreak</p>
        </div>

      </div>
    </footer>
  )
}

export default Footer