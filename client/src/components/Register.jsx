import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registerUser } from '../api';

const Register = () => {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [secondPassword, setSecondPassword] = useState("");

  const navigate = useNavigate();

  const rules = [
    { label: "5-20 characters", met: password.length >= 5 && password.length <= 20 },
    { label: "One lowercase character", met: /[a-z]/.test(password) },
    { label: "One uppercase character", met: /[A-Z]/.test(password) },
    { label: "One special character (!@#$^&)", met: /[!@#$^&]/.test(password) },
    { label: "No whitespace", met: password.length > 0 && !/\s/.test(password) },
  ]

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== secondPassword) return;
    if (rules.some(r => !r.met)) return;

    const registerSubmission = {
      email: email,
      username: username,
      password: password
    }

    try {
      const response = await registerUser(registerSubmission);
      console.log(response);

      if (response.token) {
        navigate('/confirmation');
      } else {
        console.log('Registration failed:', response);
      }
    } catch (err) {
      console.error('Error:', err);
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <div className="bg-gray-900 p-8 rounded-xl shadow-lg w-full max-w-md">
        <h2 className="text-2xl font-bold text-white mb-6">Create Account</h2>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <input
            type="text"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="px-4 py-2 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500"
          />
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="px-4 py-2 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500"
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="px-4 py-2 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500"
          />

          {password.length > 0 && (
            <ul className="text-xs flex flex-col gap-1 pl-1">
              {rules.map((rule, i) => (
                <li key={i} className={rule.met ? "text-green-400" : "text-gray-400"}>
                  {rule.met ? "✓" : "○"} {rule.label}
                </li>
              ))}
            </ul>
          )}

          <input
            type="password"
            placeholder="Retype password"
            value={secondPassword}
            onChange={(e) => setSecondPassword(e.target.value)}
            className="px-4 py-2 rounded bg-gray-700 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500"
          />

          {secondPassword.length > 0 && password !== secondPassword && (
            <p className="text-red-400 text-xs">Passwords do not match</p>
          )}

          <button
            type="submit"
            className="bg-green-600 hover:bg-green-700 text-white font-semibold py-2 rounded transition"
          >
            Register New User
          </button>
        </form>

        <p className="text-gray-400 text-sm mt-4 text-center">
          Already have an account?{" "}
          <span
            onClick={() => navigate('/login')}
            className="text-green-400 cursor-pointer hover:underline"
          >
            Login here
          </span>
        </p>
      </div>
    </div>
  )
}

export default Register