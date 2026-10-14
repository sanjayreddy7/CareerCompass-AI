  import { useEffect, useMemo, useState } from 'react'
  import {
    Activity, ArrowRight, ArrowUpRight, Bell, Bookmark, BriefcaseBusiness,
    Building2, Check, ChevronDown, CircleHelp, Clock3, FileText, LayoutDashboard,
    LogOut, MapPin, Menu, Pencil, Plus, Search, Send, Settings2, SlidersHorizontal, Sparkles,
    TrendingUp, Trash2, Upload, UserRound, Users, X,
  } from 'lucide-react'
  import { useLocation, useNavigate } from 'react-router-dom'
  import api, { errorMessage } from '../services/api'
  import { useAuth } from '../context/AuthContext'
  import '../styles/EmptyMatch.css'
  import '../styles/Home.css'

  const demoProfile = {
    id: 1, name: 'Alex Morgan', email: 'demo@careercompass.dev', location: 'Bengaluru',
    preferredRole: 'Java Developer', experience: 2, education: 'B.Tech, Computer Science',
    skills: ['Java', 'Spring Boot', 'MySQL', 'React', 'REST APIs'], role: 'USER',
  }

  const demoJobs = [
    { id: 1, title: 'Java Backend Engineer', company: 'Northstar Labs', location: 'Bengaluru, India', salary: 1200000, experienceRequired: 2, requiredSkills: ['Java', 'Spring Boot', 'REST APIs', 'MySQL', 'Docker'], description: 'Build reliable APIs and distributed services for our product platform. Work with Java, Spring Boot, REST APIs, MySQL, Docker and cloud deployments.' },
    { id: 2, title: 'Full Stack Developer', company: 'Olive Systems', location: 'Remote, India', salary: 1450000, experienceRequired: 2, requiredSkills: ['Java', 'Spring Boot', 'React', 'PostgreSQL', 'Testing'], description: 'Ship customer-facing applications across the stack. Build React interfaces and Java Spring Boot services backed by PostgreSQL and automated tests.' },
    { id: 3, title: 'Machine Learning Associate', company: 'Lattice AI', location: 'Hyderabad, India', salary: 1100000, experienceRequired: 1, requiredSkills: ['Python', 'Machine Learning', 'NLP', 'SQL', 'AWS'], description: 'Prepare datasets, evaluate models and deploy Python machine learning workflows. Familiarity with NLP, scikit-learn and cloud tooling is valued.' },
    { id: 4, title: 'Frontend Engineer', company: 'Brightworks', location: 'Pune, India', salary: 1000000, experienceRequired: 1, requiredSkills: ['React', 'JavaScript', 'TypeScript', 'CSS', 'Accessibility'], description: 'Create accessible web experiences with React and TypeScript. Partner with design and API teams to improve performance and quality.' },
    { id: 5, title: 'Cloud Platform Developer', company: 'Aster Digital', location: 'Chennai, India', salary: 1800000, experienceRequired: 3, requiredSkills: ['Java', 'Spring Boot', 'AWS', 'Docker', 'Kubernetes'], description: 'Develop cloud-native Java services and deployment pipelines using Spring Boot, AWS, Docker, Kubernetes and observability tools.' },
    { id: 6, title: 'Data Analyst', company: 'Fieldnote', location: 'Remote, India', salary: 850000, experienceRequired: 0, requiredSkills: ['SQL', 'Python', 'Excel', 'Data Analysis', 'Tableau'], description: 'Translate business questions into reliable metrics. Write SQL, create dashboards and communicate findings with product teams.' },
    { id: 7, title: 'QA Automation Engineer', company: 'Copperline Tech', location: 'Bengaluru, India', salary: 950000, experienceRequired: 1, requiredSkills: ['Java', 'Selenium', 'Testing', 'REST APIs', 'CI/CD'], description: 'Build maintainable test automation for web services using Java, Selenium, REST APIs and CI pipelines.' },
    { id: 8, title: 'Product Software Engineer', company: 'Common Thread', location: 'Mumbai, India', salary: 1600000, experienceRequired: 2, requiredSkills: ['Java', 'Spring Boot', 'React', 'PostgreSQL', 'AWS'], description: 'Work across backend services and web product features with Java, Spring Boot, React, PostgreSQL and cloud infrastructure.' },
  ]

  const menuItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'discover', label: 'Explore jobs', icon: Search },
    { id: 'recommended', label: 'For you', icon: Sparkles },
    { id: 'saved', label: 'Saved jobs', icon: Bookmark },
    { id: 'applications', label: 'Applications', icon: Send },
  ]

  function matchFallback(job, skills) {
    const names = new Set((skills || []).map((skill) => skill.toLowerCase()))
    const matches = (job.requiredSkills || []).filter((skill) => names.has(skill.toLowerCase()))
    return {
      job,
      matchScore: job.requiredSkills?.length ? Math.round((matches.length / job.requiredSkills.length) * 100) : 0,
      matchingSkills: matches,
      missingSkills: (job.requiredSkills || []).filter((skill) => !names.has(skill.toLowerCase())),
    }
  }

  function Home() {
    const navigate = useNavigate()
    const location = useLocation()
    const { logout: authLogout, updateUser } = useAuth()
    const [view, setView] = useState(location.state?.view || 'overview')
    const [profile, setProfile] = useState(demoProfile)
    const [jobs, setJobs] = useState(demoJobs)
    const [recommendations, setRecommendations] = useState([])
    const [saved, setSaved] = useState([])
    const [applications, setApplications] = useState([])
    const [query, setQuery] = useState('')
    const [locationFilter, setLocationFilter] = useState('')
    const [salaryFilter, setSalaryFilter] = useState('')
    const [selectedJob, setSelectedJob] = useState(null)
    const [notice, setNotice] = useState('')
    const [loading, setLoading] = useState(true)
    const [demoMode, setDemoMode] = useState(false)
    const [mobileMenu, setMobileMenu] = useState(false)
    const [profileForm, setProfileForm] = useState({ ...demoProfile, skills: demoProfile.skills.join(', ') })
    const [resume, setResume] = useState(null)
    const [adminJobs, setAdminJobs] = useState({ title: '', company: '', description: '', location: '', salary: '', experienceRequired: '', requiredSkills: '' })
    const [adminStats, setAdminStats] = useState(null)
    const [adminUsers, setAdminUsers] = useState([])
    const [adminApplications, setAdminApplications] = useState([])
    const [editJobId, setEditJobId] = useState(null)

    useEffect(() => {
      let active = true
      async function load() {
        const results = await Promise.allSettled([
          api.get('/users/profile'), api.get('/jobs'), api.get('/recommendations'), api.get('/jobs/saved'), api.get('/applications'), api.get('/resumes/latest'),
        ])
        if (!active) return
        const [profileResult, jobsResult, recommendationResult, savedResult, applicationResult, resumeResult] = results
        if (profileResult.status === 'fulfilled') {
          const nextProfile = profileResult.value.data
          setProfile(nextProfile)
          setProfileForm({ ...nextProfile, skills: (nextProfile.skills || []).join(', ') })
          updateUser(nextProfile)
        } else {
          setDemoMode(true)
        }
        if (jobsResult.status === 'fulfilled') setJobs(jobsResult.value.data)
        else setDemoMode(true)
        if (recommendationResult.status === 'fulfilled') setRecommendations(recommendationResult.value.data)
        if (savedResult.status === 'fulfilled') setSaved(savedResult.value.data.map((item) => item.job))
        if (applicationResult.status === 'fulfilled') setApplications(applicationResult.value.data)
        if (resumeResult.status === 'fulfilled') setResume(resumeResult.value.data)
        setLoading(false)
      }
      load()
      return () => { active = false }
    }, [updateUser])

    const hasSkills = (profile.skills || []).some((skill) => skill.trim().length > 0)
    const scoredJobs = useMemo(() => {
      const generated = jobs.map((job) => {
        const existing = hasSkills && recommendations.find((item) => item.job.id === job.id)
        return existing || matchFallback(job, profile.skills)
      })
      return generated.sort((first, second) => second.matchScore - first.matchScore)
    }, [jobs, profile.skills, recommendations, hasSkills])

    const filteredJobs = useMemo(() => jobs.filter((job) => {
      const text = `${job.title} ${job.company} ${(job.requiredSkills || []).join(' ')}`.toLowerCase()
      return (!query || text.includes(query.toLowerCase()))
        && (!locationFilter || job.location.toLowerCase().includes(locationFilter.toLowerCase()))
        && (!salaryFilter || (job.salary || 0) >= Number(salaryFilter))
    }), [jobs, query, locationFilter, salaryFilter])

    function flash(message) {
      setNotice(message)
      window.setTimeout(() => setNotice(''), 3200)
    }

    function logout() {
      authLogout()
      navigate('/login')
    }

    async function toggleSaved(job) {
      const exists = saved.some((item) => item.id === job.id)
      try {
        await api.request({ method: exists ? 'delete' : 'post', url: `/jobs/${job.id}/save` })
        setSaved((current) => exists ? current.filter((item) => item.id !== job.id) : [job, ...current])
        flash(exists ? 'Removed from saved jobs' : 'Job saved to your list')
      } catch (error) {
        flash(errorMessage(error))
      }
    }

    async function applyToJob(job) {
      if (applications.some((item) => item.job?.id === job.id)) { flash('You have already applied to this role'); return }
      try {
        const { data } = await api.post(`/jobs/${job.id}/apply`)
        setApplications((current) => [data, ...current])
        setSelectedJob(null)
        flash('Application added to your tracker')
      } catch (error) {
        flash(errorMessage(error))
      }
    }

    async function refreshRecommendations() {
      try {
        const { data } = await api.get('/recommendations?refresh=true')
        setRecommendations(data)
      } catch (error) { flash(errorMessage(error)) }
      setView('recommended')
    }

    async function saveProfile(event) {
      event.preventDefault()
      const payload = { ...profileForm, experience: profileForm.experience === '' ? 0 : Number(profileForm.experience), skills: profileForm.skills.split(',').map((skill) => skill.trim()).filter(Boolean) }
      try {
        const { data } = await api.put('/users/profile', payload)
        setProfile(data)
        setProfileForm({ ...data, skills: data.skills.join(', ') })
        updateUser(data)
        try {
          const refreshed = await api.get('/recommendations?refresh=true')
          setRecommendations(refreshed.data)
          flash('Your profile and recommendations are up to date')
        } catch {
          setRecommendations([])
          flash('Profile saved. Refresh recommendations to update your matches.')
        }
      } catch (error) {
        flash(errorMessage(error))
      }
    }

    async function uploadResume(event) {
      const file = event.target.files?.[0]
      if (!file) return
      if (file.type !== 'application/pdf' || file.size > 5 * 1024 * 1024) { flash('Choose a PDF smaller than 5 MB'); return }
      const body = new FormData()
      body.append('file', file)
      let uploaded
      try {
        const { data } = await api.post('/resumes/upload', body)
        uploaded = data
        setResume(data)
      } catch (error) {
        flash(errorMessage(error))
        return
      }

      try {
        const refreshed = await api.get('/users/profile')
        setProfile(refreshed.data)
        setProfileForm({ ...refreshed.data, skills: refreshed.data.skills.join(', ') })
      } catch (error) {
        flash(`Resume analyzed; profile refresh failed: ${errorMessage(error)}`)
        return
      }

      try {
        const matches = await api.get('/recommendations?refresh=true')
        setRecommendations(matches.data)
        flash(`${uploaded.extractedSkills.length} skills found; recommendations updated`)
      } catch {
        setRecommendations([])
        flash(`${uploaded.extractedSkills.length} skills found. Refresh recommendations to update matches.`)
      }
    }

    async function createJob(event) {
      event.preventDefault()
      const payload = { ...adminJobs, salary: Number(adminJobs.salary), experienceRequired: Number(adminJobs.experienceRequired), requiredSkills: adminJobs.requiredSkills.split(',').map((skill) => skill.trim()).filter(Boolean) }
      try {
        const { data } = await api.request({ method: editJobId ? 'put' : 'post', url: editJobId ? `/jobs/${editJobId}` : '/jobs', data: payload })
        setJobs((current) => editJobId ? current.map((job) => job.id === data.id ? data : job) : [data, ...current])
        setAdminJobs({ title: '', company: '', description: '', location: '', salary: '', experienceRequired: '', requiredSkills: '' })
        setEditJobId(null)
        flash(editJobId ? 'Job listing updated' : 'Job listing published')
      } catch (error) { flash(errorMessage(error)) }
    }

    function editJob(job) {
      setEditJobId(job.id)
      setAdminJobs({ ...job, salary: String(job.salary ?? ''), experienceRequired: String(job.experienceRequired ?? ''), requiredSkills: (job.requiredSkills || []).join(', ') })
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    async function deleteJob(job) {
      if (!window.confirm(`Delete ${job.title} at ${job.company}?`)) return
      try {
        await api.delete(`/jobs/${job.id}`)
        setJobs((current) => current.filter((item) => item.id !== job.id))
        flash('Job listing deleted')
      } catch (error) { flash(errorMessage(error)) }
    }

    async function openAdmin() {
      setView('admin')
      const [stats, usersResult, applicationsResult] = await Promise.allSettled([api.get('/admin/stats'), api.get('/admin/users'), api.get('/admin/applications')])
      if (stats.status === 'fulfilled') setAdminStats(stats.value.data)
      else { setAdminStats({ users: 1, applications: applications.length, recommendations: recommendations.length }); setDemoMode(true) }
      if (usersResult.status === 'fulfilled') setAdminUsers(usersResult.value.data)
      if (applicationsResult.status === 'fulfilled') setAdminApplications(applicationsResult.value.data)
    }

    const activeLabel = menuItems.find((item) => item.id === view)?.label || ({ profile: 'Your profile', resume: 'Resume analysis', gaps: 'Skill gaps', admin: 'Admin workspace' }[view])
    const firstName = (profile.name || 'there').split(' ')[0]

    function JobCard({ item, rank }) {
      const job = item.job || item
      const score = item.matchScore ?? matchFallback(job, profile.skills).matchScore
      const matches = item.matchingSkills ?? matchFallback(job, profile.skills).matchingSkills
      const isSaved = saved.some((entry) => entry.id === job.id)
      return (
        <article className="job-card">
          <div className="job-brand" aria-hidden="true">{job.company.split(/\s+/).map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</div>
          <div className="job-main">
            <div className="job-card-top"><div><div className="job-title-line">{rank && <span className="rank-index">{String(rank).padStart(2, '0')}</span>}<button className="job-title-button" onClick={() => setSelectedJob(item)}>{job.title}</button></div><p>{job.company}<span className="job-separator">·</span><MapPin size={13} />{job.location}</p></div>
              <button className={`icon-button save-control ${isSaved ? 'is-saved' : ''}`} aria-label={isSaved ? 'Unsave job' : 'Save job'} title={isSaved ? 'Unsave job' : 'Save job'} onClick={() => toggleSaved(job)}><Bookmark size={17} fill={isSaved ? 'currentColor' : 'none'} /></button>
            </div>
            <div className="job-skills">{(job.requiredSkills || []).slice(0, 5).map((skill) => <span className={matches.includes(skill) ? 'skill-tag skill-match' : 'skill-tag'} key={skill}>{matches.includes(skill) && <Check size={11} />}{skill}</span>)}</div>
            <div className="job-card-bottom"><div className="job-meta"><span><BriefcaseBusiness size={13} /> {job.experienceRequired ?? 0}+ yrs</span><span><TrendingUp size={13} /> ₹{((job.salary || 0) / 100000).toFixed(1)} LPA</span></div>{hasSkills && <button className="match-pill" onClick={() => setSelectedJob(item)}><span className="match-track"><span style={{ width: `${score}%` }} /></span><strong>{score}%</strong><span>match</span></button>}</div>
          </div>
        </article>
      )
    }

    function JobCollection({ items, emptyTitle, emptyText }) {
      if (!items.length) return <div className="empty-state"><div><BriefcaseBusiness size={20} /></div><h3>{emptyTitle}</h3><p>{emptyText}</p><button className="button button-outline" onClick={() => setView('discover')}>Browse open roles <ArrowRight size={15} /></button></div>
      return <div className="job-list">{items.map((item, index) => <JobCard item={item} rank={index + 1} key={item.job?.id || item.id} />)}</div>
    }

    function DashboardContent() {
      if (view === 'overview') return (
        <>
          <section className="welcome-banner"><div className="welcome-copy"><span className="eyebrow eyebrow-light">WEDNESDAY, OCTOBER 7</span><h1>Good morning, {firstName}.</h1><p>Your next good fit is closer than you think.</p><button className="button button-coral" onClick={() => setView('discover')}>Explore open roles <ArrowRight size={16} /></button></div><div className="welcome-art"><span className="orbit orbit-one" /><span className="orbit orbit-two" /><div className="compass-disc"><span className="compass-north">N</span><span className="compass-needle" /><span className="compass-center" /></div>{hasSkills && <div className="match-float"><Sparkles size={14} /><span><b>{scoredJobs[0]?.matchScore || 0}%</b> best fit</span></div>}</div></section>
          <section className="metric-row"><Metric icon={Sparkles} label="Recommended roles" value={scoredJobs.length} foot="Based on your profile" tone="mint" /><Metric icon={TrendingUp} label="Best match" value={hasSkills ? `${scoredJobs[0]?.matchScore || 0}%` : '—'} foot={hasSkills ? (scoredJobs[0]?.job?.title || 'Add your skills to begin') : 'Add skills to see matches'} tone="coral" /><Metric icon={Bookmark} label="Saved jobs" value={saved.length} foot="Keep good options close" tone="blue" /><Metric icon={Send} label="Applications" value={applications.length} foot="Your search, in motion" tone="gold" /></section>
          <div className="overview-grid"><section className="panel recommendation-panel"><div className="section-heading"><div><span className="eyebrow">CURATED FOR YOU</span><h2>Strong fits, right now</h2></div><button className="quiet-link" onClick={() => setView('recommended')}>All recommendations <ArrowRight size={15} /></button></div><JobCollection items={scoredJobs.slice(0, 3)} emptyTitle="Add a few skills first" emptyText="Your recommendations use your profile and resume to find relevant work." /></section>
            <aside className="panel profile-snapshot"><div className="section-heading"><div><span className="eyebrow">YOUR TOOLKIT</span><h2>Skills in focus</h2></div><button className="icon-button" onClick={() => setView('profile')} aria-label="Edit profile"><Settings2 size={17} /></button></div><div className="skill-cloud">{(profile.skills || []).slice(0, 8).map((skill) => <span className="skill-tag skill-match" key={skill}><Check size={11} />{skill}</span>)}{!profile.skills?.length && <p className="muted-small">Add your skills to unlock tailored matches.</p>}</div><div className="profile-completion"><div className="completion-copy"><span>Profile strength</span><strong>{Math.min(100, 30 + (profile.skills?.length || 0) * 9 + (profile.education ? 12 : 0))}%</strong></div><div className="completion-track"><span style={{ width: `${Math.min(100, 30 + (profile.skills?.length || 0) * 9 + (profile.education ? 12 : 0))}%` }} /></div><button className="quiet-link" onClick={() => setView('profile')}>Strengthen your profile <ArrowRight size={14} /></button></div></aside></div>
          <section className="panel skill-gap-preview"><div className="gap-symbol"><Activity size={18} /></div><div><span className="eyebrow">A SMALL NEXT STEP</span><h2>Build your edge</h2><p>{!hasSkills ? 'Add skills to your profile to see what could strengthen your matches.' : scoredJobs[0]?.missingSkills?.length ? `${scoredJobs[0].missingSkills.slice(0, 2).join(' and ')} could strengthen your top match.` : 'Your profile is ready for a fresh round of recommendations.'}</p></div><button className="button button-outline" onClick={() => setView('gaps')}>View skill gaps <ArrowRight size={15} /></button></section>
        </>
      )

      if (view === 'discover') return <><PageHeading eyebrow="THE OPEN MARKET" title="Find your kind of work" subtitle="Explore roles that fit the work you want to do." /><div className="filters-row"><label className="search-field"><Search size={17} /><input placeholder="Role, company or skill" value={query} onChange={(event) => setQuery(event.target.value)} /><kbd>/</kbd></label><label className="filter-field"><MapPin size={16} /><input placeholder="Location" value={locationFilter} onChange={(event) => setLocationFilter(event.target.value)} /></label><label className="filter-field filter-select"><SlidersHorizontal size={16} /><select value={salaryFilter} onChange={(event) => setSalaryFilter(event.target.value)}><option value="">Any salary</option><option value="800000">₹8 LPA+</option><option value="1200000">₹12 LPA+</option><option value="1600000">₹16 LPA+</option></select><ChevronDown size={14} /></label><span className="result-count">{filteredJobs.length} roles</span></div><JobCollection items={filteredJobs.map((job) => scoredJobs.find((entry) => entry.job.id === job.id) || matchFallback(job, profile.skills))} emptyTitle="No roles found" emptyText="Try another title, skill or location." /></>

      if (view === 'recommended') return <><PageHeading eyebrow="YOUR MATCHBOARD" title="Made for your next move" subtitle="Ranked against the skills and experience in your profile." action={<button className="button button-outline" onClick={refreshRecommendations}><Sparkles size={15} /> Refresh matches</button>} /><div className="recommendation-note"><Sparkles size={17} /><span>Match scores combine TF-IDF relevance with required-skill coverage.</span></div><JobCollection items={scoredJobs} emptyTitle="Your shortlist is waiting" emptyText="Add profile skills or upload a resume to generate useful recommendations." /></>

      if (view === 'saved') return <><PageHeading eyebrow="KEEP CLOSE" title="Saved jobs" subtitle="The roles you want to come back to." /><JobCollection items={saved.map((job) => scoredJobs.find((entry) => entry.job.id === job.id) || matchFallback(job, profile.skills))} emptyTitle="Nothing saved just yet" emptyText="Save a role to keep it on your shortlist." /></>

      if (view === 'applications') return <><PageHeading eyebrow="YOUR SEARCH, IN MOTION" title="Application tracker" subtitle="A simple record of the opportunities you’ve pursued." /><div className="application-list">{applications.map((application) => <article className="application-row" key={application.id}><div className="job-brand">{application.job.company.split(/\s+/).map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</div><div className="application-job"><strong>{application.job.title}</strong><span>{application.job.company} · {application.job.location}</span></div><span className="status-tag"><span />{application.status || 'APPLIED'}</span><time>{new Date(application.createdAt || application.appliedAt).toLocaleDateString()}</time><button className="icon-button" onClick={() => setSelectedJob(application.job)} aria-label="View job"><ArrowUpRight size={16} /></button></article>)}{!applications.length && <EmptyBlock icon={Send} title="No applications yet" text="When you apply to a role, it will appear here." />}</div></>

      if (view === 'profile') return <><PageHeading eyebrow="THE PERSON BEHIND THE PROFILE" title="Your profile" subtitle="A fuller profile helps us make more relevant recommendations." /><form className="profile-form panel" onSubmit={saveProfile}><div className="profile-form-grid"><label>Full name<input value={profileForm.name || ''} onChange={(event) => setProfileForm({ ...profileForm, name: event.target.value })} required /></label><label>Email address<input value={profileForm.email || ''} disabled /></label><label>Preferred role<input value={profileForm.preferredRole || ''} onChange={(event) => setProfileForm({ ...profileForm, preferredRole: event.target.value })} placeholder="e.g. Java Developer" /></label><label>Location<input value={profileForm.location || ''} onChange={(event) => setProfileForm({ ...profileForm, location: event.target.value })} placeholder="City or remote" /></label><label>Experience (years)<input type="number" min="0" value={profileForm.experience ?? ''} onChange={(event) => setProfileForm({ ...profileForm, experience: event.target.value })} /></label><label>Education<input value={profileForm.education || ''} onChange={(event) => setProfileForm({ ...profileForm, education: event.target.value })} placeholder="Degree or qualification" /></label><label className="wide-field">Skills <span className="field-hint">Separate each skill with a comma</span><textarea rows="3" value={profileForm.skills || ''} onChange={(event) => setProfileForm({ ...profileForm, skills: event.target.value })} placeholder="Java, Spring Boot, SQL" /></label></div><div className="form-footer"><span>Your account is private to you.</span><button className="button button-dark">Save profile <Check size={16} /></button></div></form></>

      if (view === 'resume') return <><PageHeading eyebrow="READ BETWEEN THE LINES" title="Resume analysis" subtitle="Upload a PDF to extract skills and sharpen your recommendations." /><section className="resume-layout"><div className="resume-uploader panel"><div className="upload-icon"><Upload size={21} /></div><h2>{resume?.fileName || 'Bring your experience into focus'}</h2><p>Upload a text-based PDF resume, up to 5 MB. We’ll extract recognized skills and add them to your profile.</p><label className="button button-dark upload-button">Choose PDF<input type="file" accept="application/pdf,.pdf" onChange={uploadResume} /></label><span className="upload-limit">PDF only · 5 MB maximum</span></div><div className="resume-analysis panel"><span className="eyebrow">SKILL EXTRACTION</span><h2>{resume ? 'What we found' : 'Your analysis will appear here'}</h2>{resume?.extractedSkills?.length ? <div className="skill-cloud">{resume.extractedSkills.map((skill) => <span className="skill-tag skill-match" key={skill}><Check size={11} />{skill}</span>)}</div> : <div className="analysis-empty"><FileText size={22} /><p>Upload a resume to see your skills, keywords and profile coverage.</p></div>}{resume && <p className="resume-date">Last analyzed {new Date(resume.uploadedAt).toLocaleDateString()}</p>}</div></section></>

      if (view === 'gaps' && !hasSkills) return <><PageHeading eyebrow="ROOM TO GROW" title="Your skill-gap map" subtitle="See which skills are already resonating and what could open more doors." /><section className="panel empty-state"><div><Activity size={19} /></div><h3>Add skills to see your skill gaps</h3><p>Your profile needs skills before we can show useful recommendations.</p><button className="button button-outline" onClick={() => setView('profile')}>Go to your profile <ArrowRight size={15} /></button></section></>

      if (view === 'gaps') return <><PageHeading eyebrow="ROOM TO GROW" title="Your skill-gap map" subtitle="See which skills are already resonating and what could open more doors." /><section className="gap-layout"><div className="panel gap-panel"><span className="eyebrow">MOST COMMON IN YOUR TOP MATCHES</span><h2>Skills to consider next</h2>{scoredJobs.slice(0, 4).flatMap((item) => item.missingSkills || []).reduce((counts, skill) => { counts[skill] = (counts[skill] || 0) + 1; return counts }, {}) && Object.entries(scoredJobs.slice(0, 4).flatMap((item) => item.missingSkills || []).reduce((counts, skill) => { counts[skill] = (counts[skill] || 0) + 1; return counts }, {})).sort((a, b) => b[1] - a[1]).slice(0, 6).map(([skill, count], index) => <div className="gap-bar-row" key={skill}><span>{skill}</span><div className="gap-bar-track"><span style={{ width: `${Math.max(24, 100 - index * 12)}%` }} /></div><small>{count} roles</small></div>)}{!scoredJobs.length && <EmptyBlock icon={Activity} title="Add profile details first" text="Skill gaps are calculated against your recommendations." />}</div><div className="panel gap-strength"><div className="score-ring" style={{ '--score': `${Math.min(100, profile.skills?.length * 12 || 0)}%` }}><div><strong>{Math.min(100, profile.skills?.length * 12 || 0)}%</strong><span>profile fit</span></div></div><h2>Solid foundations</h2><p>You already bring {profile.skills?.length || 0} skills into the market. Keep adding evidence through projects and experience.</p><button className="quiet-link" onClick={() => setView('profile')}>Update your profile <ArrowRight size={14} /></button></div></section></>

      return <><PageHeading eyebrow="ADMINISTRATION" title="Admin workspace" subtitle="Manage the job catalogue and review platform activity." /><section className="metric-row admin-metrics"><Metric icon={Users} label="Registered users" value={adminStats?.users ?? '—'} foot="Across the platform" tone="mint" /><Metric icon={BriefcaseBusiness} label="Open roles" value={jobs.length} foot="Available listings" tone="coral" /><Metric icon={Send} label="Applications" value={adminStats?.applications ?? applications.length} foot="Submitted by candidates" tone="blue" /><Metric icon={Sparkles} label="Matches created" value={adminStats?.recommendations ?? recommendations.length} foot="Recommendation records" tone="gold" /></section><section className="panel admin-create"><div className="section-heading"><div><span className="eyebrow">ADD TO THE MARKET</span><h2>Publish a job listing</h2></div></div><form onSubmit={createJob} className="admin-form"><label>Job title<input required value={adminJobs.title} onChange={(event) => setAdminJobs({ ...adminJobs, title: event.target.value })} /></label><label>Company<input required value={adminJobs.company} onChange={(event) => setAdminJobs({ ...adminJobs, company: event.target.value })} /></label><label>Location<input required value={adminJobs.location} onChange={(event) => setAdminJobs({ ...adminJobs, location: event.target.value })} /></label><label>Salary (annual INR)<input type="number" value={adminJobs.salary} onChange={(event) => setAdminJobs({ ...adminJobs, salary: event.target.value })} /></label><label>Experience required<input type="number" value={adminJobs.experienceRequired} onChange={(event) => setAdminJobs({ ...adminJobs, experienceRequired: event.target.value })} /></label><label>Required skills<input placeholder="Java, Spring Boot" value={adminJobs.requiredSkills} onChange={(event) => setAdminJobs({ ...adminJobs, requiredSkills: event.target.value })} /></label><label className="wide-field">Description<textarea required rows="3" value={adminJobs.description} onChange={(event) => setAdminJobs({ ...adminJobs, description: event.target.value })} /></label><div className="wide-field admin-submit"><button className="button button-dark"><Plus size={16} /> Publish role</button></div></form></section><section className="panel admin-list"><div className="section-heading"><div><span className="eyebrow">CATALOGUE</span><h2>Current roles</h2></div></div>{jobs.map((job) => <div className="admin-job-row" key={job.id}><div><strong>{job.title}</strong><span>{job.company} · {job.location}</span></div><span>{job.requiredSkills?.length || 0} skills</span></div>)}</section></>
    }

    return (
      <div className="app-shell">
        <aside className={`sidebar ${mobileMenu ? 'sidebar-open' : ''}`}>
          <button className="brand-lockup sidebar-brand" onClick={() => setView('overview')}><span className="brand-mark"><BriefcaseBusiness size={18} /></span><span className="brand-name">AI Job <span className="brand-light">Recommendation System</span></span></button>
          <div className="side-section"><span className="side-caption">YOUR SPACE</span>{menuItems.map(({ id, label, icon: Icon }) => <button className={`nav-item ${view === id ? 'nav-active' : ''}`} key={id} onClick={() => { setView(id); setMobileMenu(false) }}><Icon size={18} /><span>{label}</span>{id === 'recommended' && <span className="nav-count">{scoredJobs.length}</span>}</button>)}</div>
          <div className="side-section side-tools"><span className="side-caption">PROFILE TOOLS</span><button className={`nav-item ${view === 'profile' ? 'nav-active' : ''}`} onClick={() => setView('profile')}><UserRound size={18} /><span>Your profile</span></button><button className={`nav-item ${view === 'resume' ? 'nav-active' : ''}`} onClick={() => setView('resume')}><FileText size={18} /><span>Resume analysis</span></button><button className={`nav-item ${view === 'gaps' ? 'nav-active' : ''}`} onClick={() => setView('gaps')}><Activity size={18} /><span>Skill gaps</span></button>{profile.role === 'ADMIN' && <button className={`nav-item ${view === 'admin' ? 'nav-active' : ''}`} onClick={openAdmin}><Settings2 size={18} /><span>Admin workspace</span></button>}</div>
          <div className="sidebar-bottom"><div className="sidebar-help"><CircleHelp size={17} /><span>Need a hand?<small>See how matching works</small></span><ArrowUpRight size={14} /></div><button className="account-row" onClick={() => setView('profile')}><span className="avatar">{profile.name?.split(' ').map((part) => part[0]).slice(0, 2).join('')}</span><span className="account-name"><strong>{profile.name}</strong><small>{profile.preferredRole || 'Job seeker'}</small></span><ChevronDown size={15} /></button><button className="nav-item signout-item" onClick={logout}><LogOut size={16} /><span>Sign out</span></button></div>
        </aside>
        <main className="main-column">
          <header className="topbar"><button className="mobile-menu icon-button" onClick={() => setMobileMenu(!mobileMenu)} aria-label="Toggle navigation">{mobileMenu ? <X size={20} /> : <Menu size={20} />}</button><div className="breadcrumbs"><span>Workspace</span><span>/</span><strong>{activeLabel}</strong></div><div className="topbar-actions"><label className="top-search"><Search size={16} /><input placeholder="Search roles..." value={query} onChange={(event) => { setQuery(event.target.value); setView('discover') }} /><kbd>⌘ K</kbd></label><button className="icon-button notification-button" aria-label="Notifications"><Bell size={18} /><i /></button><button className="top-avatar" onClick={() => setView('profile')}>{profile.name?.split(' ').map((part) => part[0]).slice(0, 2).join('')}</button></div></header>
          <div className="page-content">{demoMode && <div className="demo-banner"><Sparkles size={15} /><span>Demo preview is showing sample roles. Connect the Spring Boot API to sync your profile and activity.</span><button onClick={() => setDemoMode(false)} aria-label="Dismiss"><X size={15} /></button></div>}{!loading && !hasSkills && <div className="empty-match-banner"><div><Sparkles size={17} /></div><p>Add your skills or upload a resume to see your matches</p><button className="button button-outline" onClick={() => setView('profile')}>Go to your profile <ArrowRight size={15} /></button></div>}{loading && <div className="loading-line"><span /> Loading your workspace</div>}{DashboardContent()}{view === 'admin' && <AdminRecords users={adminUsers} applications={adminApplications} jobs={jobs} onEdit={editJob} onDelete={deleteJob} />}</div>
        </main>
        {selectedJob && <JobModal item={selectedJob} saved={saved.some((job) => job.id === (selectedJob.job?.id || selectedJob.id))} hasSkills={hasSkills} onClose={() => setSelectedJob(null)} onProfile={() => { setSelectedJob(null); setView('profile') }} onSave={() => toggleSaved(selectedJob.job || selectedJob)} onApply={() => applyToJob(selectedJob.job || selectedJob)} />}
        {notice && <div className="toast"><Check size={16} />{notice}</div>}
      </div>
    )
  }

  function Metric({ icon: Icon, label, value, foot, tone }) {
    return <article className="metric-card"><div className={`metric-icon ${tone}`}><Icon size={17} /></div><span className="metric-label">{label}</span><strong className="metric-value">{value}</strong><span className="metric-foot">{foot}</span></article>
  }

  function PageHeading({ eyebrow, title, subtitle, action }) {
    return <div className="page-heading"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{subtitle}</p></div>{action}</div>
  }

  function EmptyBlock({ icon: Icon, title, text }) {
    return <div className="empty-state"><div><Icon size={19} /></div><h3>{title}</h3><p>{text}</p></div>
  }

  function AdminRecords({ users, applications, jobs, onEdit, onDelete }) {
    return <>
      <section className="panel admin-list"><div className="section-heading"><div><span className="eyebrow">JOB CATALOGUE</span><h2>Manage listings</h2></div></div>{jobs.map((job) => <div className="admin-job-row" key={job.id}><div><strong>{job.title}</strong><span>{job.company} · {job.location}</span></div><div className="admin-row-actions"><button className="icon-button" onClick={() => onEdit(job)} aria-label={`Edit ${job.title}`} title="Edit listing"><Pencil size={15} /></button><button className="icon-button danger-action" onClick={() => onDelete(job)} aria-label={`Delete ${job.title}`} title="Delete listing"><Trash2 size={15} /></button></div></div>)}</section>
      <section className="admin-report-grid"><div className="panel admin-list"><div className="section-heading"><div><span className="eyebrow">PEOPLE</span><h2>Registered users</h2></div><span className="report-count">{users.length}</span></div>{users.map((user) => <div className="admin-job-row" key={user.id}><div><strong>{user.name}</strong><span>{user.email}</span></div><span>{user.role}</span></div>)}{!users.length && <p className="muted-small">No account records loaded.</p>}</div><div className="panel admin-list"><div className="section-heading"><div><span className="eyebrow">CANDIDATE ACTIVITY</span><h2>Applications</h2></div><span className="report-count">{applications.length}</span></div>{applications.slice(0, 8).map((application) => <div className="admin-job-row" key={application.id}><div><strong>{application.job.title}</strong><span>{application.job.company}</span></div><span>{application.status}</span></div>)}{!applications.length && <p className="muted-small">No applications have been submitted.</p>}</div></section>
    </>
  }

  function JobModal({ item, saved, hasSkills, onClose, onProfile, onSave, onApply }) {
    const job = item.job || item
    const score = item.matchScore ?? 0
    const matches = item.matchingSkills || []
    const missing = item.missingSkills || []
    return <div className="modal-scrim" onClick={onClose}><section className="job-modal" role="dialog" aria-modal="true" aria-label={`${job.title} details`} onClick={(event) => event.stopPropagation()}><div className="modal-top"><div className="job-brand job-brand-large">{job.company.split(/\s+/).map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</div><button className="icon-button" onClick={onClose} aria-label="Close details"><X size={19} /></button></div><span className="eyebrow">ROLE DETAILS</span><h2>{job.title}</h2><p className="modal-company"><Building2 size={15} />{job.company}<span>·</span><MapPin size={14} />{job.location}</p><div className="modal-facts"><span><TrendingUp size={14} />₹{((job.salary || 0) / 100000).toFixed(1)} LPA</span><span><BriefcaseBusiness size={14} />{job.experienceRequired ?? 0}+ years</span><span><Clock3 size={14} />Full time</span></div>{hasSkills ? <div className="modal-score"><div className="modal-score-number">{score}<small>%</small></div><div><strong>Your match score</strong><span>Profile fit based on your skills and experience</span></div><div className="score-track"><i style={{ width: `${score}%` }} /></div></div> : <div className="modal-empty-match"><p>Add your skills or upload a resume to see your matches.</p><button className="quiet-link" onClick={onProfile}>Go to your profile <ArrowRight size={14} /></button></div>}<p className="job-description">{job.description}</p><div className="modal-skill-groups"><div><h3><Check size={14} /> Skills you bring</h3><div className="skill-cloud">{matches.length ? matches.map((skill) => <span className="skill-tag skill-match" key={skill}>{skill}</span>) : <span className="muted-small">No direct skill matches yet</span>}</div></div><div><h3><ArrowUpRight size={14} /> Skills to build</h3><div className="skill-cloud">{missing.length ? missing.map((skill) => <span className="skill-tag" key={skill}>{skill}</span>) : <span className="muted-small">You cover the listed skills</span>}</div></div></div>{item.explanation && <div className="recommendation-explanation"><Sparkles size={15} /><p>{item.explanation}</p></div>}<div className="modal-actions"><button className="button button-outline" onClick={onSave}><Bookmark size={15} fill={saved ? 'currentColor' : 'none'} />{saved ? 'Saved' : 'Save job'}</button><button className="button button-dark" onClick={onApply}>Apply for this role <ArrowUpRight size={16} /></button></div></section></div>
  }

  export default Home