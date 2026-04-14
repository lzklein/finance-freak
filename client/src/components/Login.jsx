import React, {useState} from 'react'
import { useNavigate } from 'react-router-dom'

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const navigate = useNavigate();

  const handleSubmit = (e) => {
    const loginSubmission = {
      username: username,
      password: password
    }

    console.log(loginSubmission);
  }

  return (
    <div>
      <form onSubmit={handleSubmit}>
        <input type="text" placeholder='Email or Username' value={username} onChange={(e)=>{setUsername(e.target.value)}}></input>
        <input type="password" placeholder='password' value={password} onChange={(e)=>{setPassword(e.target.value)}}></input>
        <button type="submit">Login</button>
      </form>
      <a onClick={() => navigate('/register')}>Don't have an account? Click here to register</a>
    </div>
  )
}

export default Login