import axios from '@/axios'

// User
export const login = (params) => axios.post('/api/user/login', params)
export const logout = () => axios.post('/api/user/logout')
export const getUserStats = () => axios.get('/api/user/stats')

// Tags
export const getTags = () => axios.get('/api/tags')

// Recommendation & Chat
export const getRecommend = (params) => axios.post('/api/recommend', params)
export const chat = (params) => axios.post('/api/chat', params)

// Place
export const getPlaceDetail = (placeId) => axios.get(`/api/place/detail/${placeId}`)
export const getTravelTips = (placeId) => axios.post(`/api/place/${placeId}/travel-tips`)

// Memory / Bookmarks
export const toggleBookmark = (placeId) => axios.post(`/api/memory/${placeId}/bookmark`)
export const getBookmarks = () => axios.get('/api/memory/bookmarks')
export const getVisitedWithMood = () => axios.get('/api/memory/visited-with-mood')

// Location
export const updateLocation = (params) => axios.post('/api/location/update', params)