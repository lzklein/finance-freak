import React, { useEffect, useState, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

const Confirmation = () => {
  const [status, setStatus] = useState('pending')
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const hasCalled = useRef(false)

  useEffect(() => {
    if (!token || hasCalled.current) return
    hasCalled.current = true

    const verify = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/auth/verify?token=${token}`
        )

        if (response.ok) {
          setStatus('success')
          setTimeout(() => navigate('/login'), 2000)
        } else {
          setStatus('error')
        }
      } catch (err) {
        setStatus('error')
      }
    }

    verify()
  }, [token, navigate])

  const hasToken = !!token

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <div className="bg-gray-900 p-8 rounded-xl shadow-lg w-full max-w-md text-center">
        
        {!hasToken ? (
          <>
            <p className="text-4xl mb-4">📧</p>
            <h2 className="text-2xl font-bold text-white mb-2">Check your email</h2>
            <p className="text-gray-400">
              We sent a verification link to your email address.
              Click the link to activate your account.
            </p>
            <p className="text-gray-500 text-sm mt-4">
              Didn't receive it?{" "}
              <span
                onClick={() => navigate('/register')}
                className="text-green-400 cursor-pointer hover:underline"
              >
                Try registering again
              </span>
            </p>
          </>
        ) : (
          <>
            {status === 'pending' && (
              <p className="text-white">Verifying your email...</p>
            )}

            {status === 'success' && (
              <>
                <p className="text-green-400 text-xl font-bold">
                  ✓ Email verified!
                </p>
                <p className="text-gray-400 mt-2">
                  Redirecting you to login...
                </p>
              </>
            )}

            {status === 'error' && (
              <>
                <p className="text-red-400 text-xl font-bold">
                  Verification failed
                </p>
                <p className="text-gray-400 mt-2">
                  Your link may have expired.
                </p>
                <button
                  onClick={() => navigate('/register')}
                  className="mt-4 bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded"
                >
                  Back to Register
                </button>
              </>
            )}
          </>
        )}

      </div>
    </div>
  )
}

export default Confirmation