import axios from '@/axios'

// User
export const login = (params) => axios.post('/user/login', params)
export const logout = () => axios.post('/user/logout')
export const getUserStats = () => axios.get('/user/stats')
export const uploadAvatar = (formData) => axios.post('/user/upload-avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
})

// Tags
export const getTags = () => axios.get('/tags')

// Recommendation & Chat
export const getRecommend = (params) => axios.post('/recommend', params)
export const chat = (params) => axios.post('/chat', params)

// Place
export const getPlaceDetail = (placeId) => axios.get(`/place/detail/${placeId}`)
export const getTravelTips = (placeId) => axios.post(`/place/${placeId}/travel-tips`)

// Memory / Bookmarks
export const toggleBookmark = (placeId, data) => axios.post(`/memory/${placeId}/bookmark`, data)
export const getBookmarks = () => axios.get('/memory/bookmarks')
export const getVisitedWithMood = () => axios.get('/memory/visited-with-mood')

// Location
export const updateLocation = (params) => axios.post('/location/update', params)

// Discovery
export const recordExploration = (placeCard) => axios.post('/memory/explore', placeCard)
export const getDiscoveryPlaces = () => axios.get('/memory/discovery')