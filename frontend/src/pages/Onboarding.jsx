import { useEffect, useState } from 'react'
import { ArrowLeft, ArrowRight, BriefcaseBusiness, Check, Upload } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../services/api'
import '../styles/Auth.css'
import '../styles/Onboarding.css'

const skillOptions = [
    'Java', 'Spring Boot', 'JavaScript', 'React', 'TypeScript', 'Python',
    'SQL', 'REST APIs', 'AWS', 'Docker', 'Machine Learning', 'Data Analysis',
    'Figma', 'UI/UX Design', 'Product Management', 'Communication',
]

function Onboarding() {
    const navigate = useNavigate()
    const [profile, setProfile] = useState(null)
    const [selectedSkills, setSelectedSkills] = useState([])
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    useEffect(() => {
        let active = true
        api.get('/users/profile')
            .then(({ data }) => {
                if (active) setProfile(data)
            })
            .catch((requestError) => {
                if (active) setError(errorMessage(requestError))
            })
        return () => { active = false }
    }, [])

    function toggleSkill(skill) {
        setSelectedSkills((current) => current.includes(skill)
            ? current.filter((item) => item !== skill)
            : [...current, skill])
    }

    async function saveSkills(event) {
        event.preventDefault()
        if (!profile || selectedSkills.length === 0) return

        setBusy(true)
        setError('')
        try {
            const { data } = await api.put('/users/profile', {
                name: profile.name,
                education: profile.education,
                experience: profile.experience ?? 0,
                location: profile.location,
                preferredRole: profile.preferredRole,
                skills: selectedSkills,
            })
            localStorage.setItem('user', JSON.stringify(data))
            navigate('/dashboard', { replace: true })
        } catch (requestError) {
            setError(errorMessage(requestError))
        } finally {
            setBusy(false)
        }
    }

    return (
        <main className="auth-shell">
            <section className="auth-story">
                <Link className="brand-lockup brand-on-dark" to="/">
                    <span className="brand-mark"><BriefcaseBusiness size={18} /></span>
                    <span className="brand-name">AI Job <span className="brand-light">Recommendation System</span></span>
                </Link>
                <div className="auth-story-copy">
                    <span className="eyebrow eyebrow-light">A MORE PERSONAL SEARCH</span>
                    <h1>Start with<br />what you know.</h1>
                    <p>A few skills are all we need to start finding roles that fit.</p>
                </div>
                <div className="story-caption"><span className="note-dot" /> Your next chapter starts here.</div>
            </section>
            <section className="auth-form-side">
                <Link className="back-home" to="/"><ArrowLeft size={15} /> Back home</Link>
                <div className="auth-form-wrap onboarding-form">
                    <span className="eyebrow">STEP 1 OF 1</span>
                    <h2>What skills do you bring?</h2>
                    <p className="auth-intro">Choose a few skills to personalize your job matches. You can edit these any time.</p>
                    <form onSubmit={saveSkills}>
                        <div className="onboarding-skills" aria-label="Choose your skills">
                            {skillOptions.map((skill) => {
                                const selected = selectedSkills.includes(skill)
                                return (
                                    <button
                                        className={`onboarding-skill${selected ? ' selected' : ''}`}
                                        type="button"
                                        key={skill}
                                        aria-pressed={selected}
                                        onClick={() => toggleSkill(skill)}
                                    >
                                        {selected && <Check size={13} />}
                                        {skill}
                                    </button>
                                )
                            })}
                        </div>
                        {error && <p className="form-error" role="alert">{error}</p>}
                        <button className="button button-dark auth-submit" disabled={busy || !profile || selectedSkills.length === 0}>
                            {busy ? 'Saving your skills…' : 'See my matches'}
                            <ArrowRight size={17} />
                        </button>
                    </form>
                    <button
                        className="onboarding-resume"
                        type="button"
                        onClick={() => navigate('/dashboard', { replace: true, state: { view: 'resume' } })}
                    >
                        <Upload size={16} /> Upload a PDF resume instead
                    </button>
                </div>
            </section>
        </main>
    )
}

export default Onboarding
