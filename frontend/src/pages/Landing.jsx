import { ArrowUpRight, BriefcaseBusiness, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import '../App.css'

function Landing() {
  return (
    <main className="landing-shell">
      <nav className="landing-nav">
        <Link className="brand-lockup" to="/"><span className="brand-mark"><BriefcaseBusiness size={18} /></span><span className="brand-name">AI Job <span className="brand-light">Recommendation System</span></span></Link>
        <div className="landing-links"><a href="#how-it-works">How it works</a><Link to="/login">Sign in</Link><Link className="button button-dark" to="/register">Get started <ArrowUpRight size={16} /></Link></div>
      </nav>
      <section className="landing-hero">
        <div className="hero-image" />
        <div className="hero-shade" />
        <div className="hero-content">
          <div className="eyebrow eyebrow-light"><Sparkles size={14} /> YOUR NEXT CHAPTER, MATCHED</div>
          <h1>Find work that<br />fits <em>who you are.</em></h1>
          <p>Turn your skills into a clearer next move. The AI Job Recommendation System reads your profile, finds the roles that fit, and shows you exactly where to grow.</p>
          <div className="hero-actions"><Link className="button button-coral" to="/register">Build your profile <ArrowUpRight size={17} /></Link><Link className="text-link-light" to="/login">I already have an account <span>→</span></Link></div>
          <div className="hero-proof"><div className="proof-avatars"><span>AM</span><span>JR</span><span>SK</span></div><span>Thoughtful matches, backed by your skills</span></div>
        </div>
        <div className="hero-note"><span className="note-dot" /> Your skills, in focus</div>
      </section>
      <section className="landing-strip" id="how-it-works">
        <span className="strip-label">A better way to find your fit</span>
        <div><strong>01</strong><span>Upload your resume</span></div><div><strong>02</strong><span>See your match score</span></div><div><strong>03</strong><span>Make your next move</span></div>
      </section>
    </main>
  )
}

export default Landing