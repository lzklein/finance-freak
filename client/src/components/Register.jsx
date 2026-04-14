import React, {useState} from 'react'
import { useNavigate } from 'react-router-dom'

const Register = () => {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [secondPassword, setSecondPassword] = useState("");

  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    if (password !== secondPassword) {
      return;
    }

    const registerSubmission = {
      email: email,
      username: username,
      password: password
    }

    console.log(registerSubmission);
  }

  return (
    <div>
      <form onSubmit={handleSubmit}>
        <input type="text" placeholder='Email' value={email} onChange={(e)=>{setEmail(e.target.value)}}></input>
        <input type="text" placeholder='Username' value={username} onChange={(e)=>{setUsername(e.target.value)}}></input>
        <p>Password must be 5-20 characters</p>
        <ul>
          <li>One lower case character</li>
          <li>One upper case character</li>
          <li>One special character (!@#$%)</li>
          <li>No Whitespace</li>
        </ul>
        <input type="password" placeholder='password' value={password} onChange={(e)=>{setPassword(e.target.value)}}></input>
        <input type="password" placeholder='retype password' value={secondPassword} onChange={(e)=>{setSecondPassword(e.target.value)}}></input>

        <button type="submit">Register New User</button>
      </form>
      <a onClick={() => navigate('/login')}>Have an account? Click here to login</a>
    </div>
  )
}

export default Register