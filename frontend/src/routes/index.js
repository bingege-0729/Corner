import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../views/LoginPage.vue'
import Home from '../views/Home.vue'
import Recommend from '../views/Recommend.vue'
import PlaceDetail from '../views/PlaceDetail.vue'
import My from '../views/My.vue'
import Memory from '../views/Memory.vue'
import Forward from '../views/Forward.vue'

const routes = [
  {
    path: '/login',
    name: 'LoginPage',
    component: LoginPage
  },
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/recommend',
    name: 'Recommend',
    component: Recommend
  },
  {
    path: '/place/:id',
    name: 'PlaceDetail',
    component: PlaceDetail
  },
  {
    path: '/forward/:id',
    name: 'Forward',
    component: Forward
  },
  {
    path: '/my',
    name: 'My',
    component: My
  },
  {
    path: '/memory',
    name: 'Memory',
    component: Memory
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router