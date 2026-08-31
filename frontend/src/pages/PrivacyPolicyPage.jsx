import { Link } from 'react-router-dom'
import { themes } from '../theme/themes'
import Wordmark from '../components/Wordmark'

const sectionTitle = { fontSize: 15, fontWeight: 650, color: 'rgba(255,255,255,0.92)', marginTop: 28, marginBottom: 8 }
const body = { fontSize: 14, lineHeight: 1.65, color: '#a0a0b0' }

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen" style={{ background: themes.brand.background }}>
      <div className="max-w-2xl mx-auto px-6 py-16">
        <Link to="/login" className="inline-block mb-10">
          <Wordmark size={20} />
        </Link>

        <h1 style={{ fontSize: 24, fontWeight: 650, color: 'rgba(255,255,255,0.92)' }}>
          Privacy Policy
        </h1>
        <p style={{ ...body, marginTop: 8, color: '#777788' }}>
          Last updated: 08/30/2026
        </p>

        <p style={{ ...body, marginTop: 20 }}>
          EntTrac is a personal entertainment-tracking app. This page explains what
          information it collects, how it's used, and how you can request it be removed.
        </p>

        <h2 style={sectionTitle}>What we collect</h2>
        <p style={body}>
          When you sign in with Google, EntTrac receives your name, email address, and
          profile picture — nothing beyond basic account identification. We don't request
          access to your Gmail, Drive, or any other Google data.
        </p>
        <p style={{ ...body, marginTop: 10 }}>
          Once signed in, EntTrac stores the media entries you add (titles, status,
          progress, ratings, and notes you write) so your library persists between visits.
        </p>

        <h2 style={sectionTitle}>How it's used</h2>
        <p style={body}>
          Your data is used solely to run your own account — displaying your library,
          keeping you signed in, and letting you search for and track new titles.
          EntTrac doesn't run ads, doesn't sell data, and doesn't share your account
          information with any third party.
        </p>

        <h2 style={sectionTitle}>Third-party services</h2>
        <p style={body}>
          Search and metadata (titles, covers, descriptions) come from TMDB, OMDb, IGDB,
          MangaDex, and Open Library. These lookups happen server-side and only ever send
          a search term or title ID — never your personal account information.
        </p>

        <h2 style={sectionTitle}>Cookies</h2>
        <p style={body}>
          EntTrac uses a single httpOnly session cookie to keep you signed in. It isn't
          used for tracking or advertising.
        </p>

        <h2 style={sectionTitle}>Deleting your data</h2>
        <p style={body}>
          To request deletion of your account and all associated data, email{' '}
          <a href="mailto:josephmiddlebrook@gmail.com" style={{ color: themes.brand.accent }}>josephmiddlebrook@gmail.com</a>.
        </p>

        <h2 style={sectionTitle}>Contact</h2>
        <p style={body}>
          Questions about this policy: <a href="mailto:josephmiddlebrook@gmail.com" style={{ color: themes.brand.accent }}>josephmiddlebrook@gmail.com</a>
        </p>
      </div>
    </div>
  )
}