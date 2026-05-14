import axios from "axios"

const instance = axios.create({
    baseURL: "/api"
})

instance.interceptors.request.use(config=>{
    const token=localStorage.getItem("token")
    if(token){
        config.headers.Authorization = 'Bearer '+token
    }
    return config
})

instance.interceptors.response.use(
    (res)=>{
        return res.data
    },
    (err)=>{
        return Promise.reject(err)
    }
)

export default instance