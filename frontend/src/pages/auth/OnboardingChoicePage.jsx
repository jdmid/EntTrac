import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { themes } from '../../theme/themes'
import Wordmark from '../../components/Wordmark'

export default function OnboardingChoicePage() {
  const { markOnboarded } = useAuth()
  const navigate = useNavigate()

  async function handleSkip() {
    await markOnboarded()
    navigate('/', { replace: true })
  }

  function handleSetUp() {
    navigate('/onboarding/preferences', { replace: true })
  }

  return (
    <div
      className="flex min-h-screen items-center justify-center px-4"
      style={{ background: themes.brand.background }}
    >
      <div className="flex flex-col items-center gap-5 text-center">
        <Wordmark size={19} />
        <p className="max-w-[240px] text-[13px]" style={{ color: '#777788' }}>
          Want to pick what you're tracking and set a display name now, or just dive in?
        </p>
        <div className="flex flex-wrap justify-center gap-2.5">
          <button
            onClick={handleSetUp}
            className="rounded px-4 py-2.5 text-[13px] font-medium"
            style={{ background: themes.brand.accent, color: '#ffffff' }}
          >
            Set Up Preferences
          </button>
          <button
            onClick={handleSkip}
            className="rounded px-4 py-2.5 text-[13px] font-medium"
            style={{
              background: themes.brand.cardCover,
              border: `0.5px solid ${themes.brand.cardBorder}`,
              color: themes.brand.accent,
            }}
          >
            Skip to App
          </button>
        </div>
      </div>
    </div>
  )
}