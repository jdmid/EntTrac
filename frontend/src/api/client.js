import axios from 'axios'

const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

let refreshPromise = null

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const isAuthEndpoint = originalRequest?.url?.includes('/auth/')

    if (error.response?.status !== 401 || isAuthEndpoint || originalRequest._retry) {
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
      window.location.href = '/login'
      return Promise.reject(refreshError)
    }
  }
)

export default client