import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return {
        code: res.code,
        message: res.message || 'success',
        data: res.data
      }
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const userApi = {
  login: (phone) => api.post('/user/login', { phone })
}

export const tagsApi = {
  getTags: () => api.get('/tags')
}

export const recommendApi = {
  getRecommend: (params) => api.post('/recommend', params)
}

export const placeApi = {
  feedback: (params) => api.post('/place/feedback', params),
  getDetail: (placeId) => api.get(`/place/detail/${placeId}`)
}

export const memoryApi = {
  getList: (type) => {
    const params = type ? { type } : {}
    return api.get('/memory/list', { params })
  }
}

export default api