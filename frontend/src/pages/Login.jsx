import { useState } from 'react'
import { ArrowLeft, ArrowRight, BriefcaseBusiness, LockKeyhole, Mail } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import '../styles/Auth.css'

function Login() {
    const navigate = useNavigate()
    const location = useLocation()
    const { login } = useAuth()
    const from = location.state?.from?.pathname || '/dashboard'

    const [values, setValues] = useState({ email: '', password: '' })
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    function update(event) {
        setValues({ ...values, [event.target.name]: event.target.value })
    }

    async function submit(event) {
        event.preventDefault()
        setBusy(true)
        setError('')
        try {
            const { data } = await api.post('/auth/login', values)
            if (!data?.token) {
                throw new Error('Authentication succeeded but no token was received from server.')
            }
            login(data.token, data.user)
            navigate(from, { replace: true })
        } catch (requestError) {
            setError(errorMessage(requestError))
        } finally {
            setBusy(false)
        }
    }

    return (
        <main className="auth-shell">
            <section className="auth-story">
                <Link className="brand-lockup brand-on-dark" to="/"><span className="brand-mark"><BriefcaseBusiness size={18} /></span><span className="brand-name">AI Job <span className="brand-light">Recommendation System</span></span></Link>
                <div className="auth-story-copy"><span className="eyebrow eyebrow-light">MAKE YOUR NEXT MOVE</span><h1>Your skills have<br />somewhere to go.</h1><p>Find opportunities that make sense for the work you want to do next.</p></div>
                <div className="story-caption"><span className="note-dot" /> A little more direction changes everything.</div>
            </section>
            <section className="auth-form-side">
                <Link className="back-home" to="/"><ArrowLeft size={15} /> Back home</Link>
                <div className="auth-form-wrap"><span className="eyebrow">WELCOME BACK</span><h2>Sign in to your account</h2><p className="auth-intro">Pick up where your next move begins.</p>
                    <form onSubmit={submit} action="javascript:void(0);">
                        <label>Email address<span className="input-wrap"><Mail size={17} /><input name="email" type="email" autoComplete="email" placeholder="you@example.com" value={values.email} onChange={update} required /></span></label>
                        <label>Password<span className="input-wrap"><LockKeyhole size={17} /><input name="password" type="password" autoComplete="current-password" placeholder="Your password" value={values.password} onChange={update} required /></span></label>
                        {error && <p className="form-error" role="alert">{error}</p>}
                        <button className="button button-dark auth-submit" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'} <ArrowRight size={17} /></button>
                    </form>
                    <p className="auth-switch">New to the AI Job Recommendation System? <Link to="/register">Create an account</Link></p>
                    <div className="demo-credentials"><strong>Demo account</strong><span>demo@careercompass.dev</span><span>DemoPass123!</span></div>
                </div>
            </section>
        </main>
    )
}

export default Login