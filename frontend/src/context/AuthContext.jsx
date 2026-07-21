import { createContext, useContext, useEffect, useState } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [status, setStatus] = useState('loading') // 'loading' | 'authenticated' | 'unauthenticated'

  useEffect(() => {
    client.get('/auth/me')
      .then((res) => {
        setUser(res.data)
        setStatus('authenticated')
      })
      .catch(() => {
        setStatus('unauthenticated')
      })
  }, [])

  async function loginWithGoogle(idToken) {
    const res = await client.post('/auth/google', { idToken })
    setUser(res.data)
    setStatus('authenticated')
    return res.data
  }

  async function logout() {
    await client.post('/auth/logout')
    setUser(null)
    setStatus('unauthenticated')
  }

  async function markOnboarded() {
    await client.patch('/auth/onboarded')
    setUser((prev) => (prev ? { ...prev, onboarded: true } : prev))
  }

  async function updateDisplayName(displayName) {
    await client.patch('/auth/profile', { displayName })
    setUser((prev) => (prev ? { ...prev, displayName } : prev))
  }

  return (
    <AuthContext.Provider value={{ user, status, loginWithGoogle, logout, markOnboarded, updateDisplayName }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}