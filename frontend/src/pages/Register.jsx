import { useState } from 'react'
import { ArrowLeft, ArrowRight, BriefcaseBusiness, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import '../styles/Auth.css'

function Register() {
    const navigate = useNavigate()
    const { login } = useAuth()
    const [values, setValues] = useState({ name: '', email: '', password: '' })
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
            const { data } = await api.post('/auth/register', values)
            if (!data?.token) {
                throw new Error('Registration succeeded but no token was received from server.')
            }
            login(data.token, data.user)
            navigate('/onboarding', { replace: true })
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
                <div className="auth-story-copy"><span className="eyebrow eyebrow-light">A GOOD PLACE TO BEGIN</span><h1>Make your skills<br />go further.</h1><p>Build your profile once. Get a more personal view of the roles worth your time.</p></div>
                <div className="story-caption"><span className="note-dot" /> Your next chapter starts here.</div>
            </section>
            <section className="auth-form-side">
                <Link className="back-home" to="/"><ArrowLeft size={15} /> Back home</Link>
                <div className="auth-form-wrap"><span className="eyebrow">CREATE YOUR PROFILE</span><h2>Find your next fit</h2><p className="auth-intro">A few details get your search moving.</p>
                    <form onSubmit={submit} action="javascript:void(0);">
                        <label>Full name<span className="input-wrap"><UserRound size={17} /><input name="name" autoComplete="name" placeholder="Your name" value={values.name} onChange={update} required /></span></label>
                        <label>Email address<span className="input-wrap"><Mail size={17} /><input name="email" type="email" autoComplete="email" placeholder="you@example.com" value={values.email} onChange={update} required /></span></label>
                        <label>Password<span className="input-wrap"><LockKeyhole size={17} /><input name="password" type="password" minLength={8} autoComplete="new-password" placeholder="At least 8 characters" value={values.password} onChange={update} required /></span></label>
                        {error && <p className="form-error" role="alert">{error}</p>}
                        <button className="button button-dark auth-submit" disabled={busy}>{busy ? 'Creating profile…' : 'Create my profile'} <ArrowRight size={17} /></button>
                    </form>
                    <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
                </div>
            </section>
        </main>
    )
}

export default Register