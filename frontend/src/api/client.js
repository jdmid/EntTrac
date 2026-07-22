import axios from 'axios'

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

let refreshPromise = null

const PUBLIC_AUTH_PATHS = ['/auth/google', '/auth/refresh', '/auth/logout']

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const isPublicAuthEndpoint = PUBLIC_AUTH_PATHS.some((path) => originalRequest?.url?.includes(path))

    if (error.response?.status !== 401 || isPublicAuthEndpoint || originalRequest._retry) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      refreshPromise = refreshPromise ?? client.post('/auth/refresh')
      await refreshPromise
      refreshPromise = null
      return client(originalRequest)
    } catch (refreshError) {
      refreshPromise = null
      window.dispatchEvent(new Event('auth:session-expired'))
      return Promise.reject(refreshError)
    }
  }
)

export default client