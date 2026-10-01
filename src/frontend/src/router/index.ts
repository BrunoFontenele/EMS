import { createRouter, createWebHistory } from 'vue-router'
import AuditLogView from '@/views/auditlog/AuditLogView.vue'
import HomeView from '../views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import PeopleView from '@/views/people/PeopleView.vue'
import SchoolView from '@/views/school/SchoolView.vue'
import SubjectView from '@/views/subject/SubjectView.vue'
import ExamView from '@/views/exams/ExamView.vue'
import ProcessExamView from '@/views/exams/ProcessExamView.vue'
import QuestionView from '@/views/questions/QuestionsView.vue'
import CorrectionViewDialog from '@/views/corrections/CorrectionViewDialog.vue'
import StudentDashboardView from '@/views/student/StudentDashboardView.vue'
import StudentExamView from '@/views/student/StudentExamView.vue'
import ReviewsView from '@/views/reviews/ReviewsView.vue'
import DashboardView from '@/views/DashboardView.vue'
import StatisticsView from '@/views/statistics/StatisticsView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { public: true }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/audit-logs',
      name: 'audit-logs',
      component: AuditLogView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/people',
      name: 'people',
      component: PeopleView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/schools',
      name: 'schools',
      component: SchoolView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/exams',
      name: 'exams',
      component: ExamView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR' || auth.user?.role === 'SCHOOL_STAFF') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/exams/:id/process',
      name: 'process-exam',
      component: ProcessExamView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR' || auth.user?.role === 'SCHOOL_STAFF') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/questions',
      name: 'questions',
      component: QuestionView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR' || auth.user?.role === 'SCHOOL_STAFF' || auth.user?.role === 'TEACHER') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/corrections',
      name: 'corrections',
      component: CorrectionViewDialog,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR' || auth.user?.role === 'TEACHER') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/student/dashboard',
      name: 'student-dashboard',
      component: StudentDashboardView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'STUDENT' || auth.user?.role === 'ADMINISTRATOR') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/reviews',
      name: 'reviews',
      component: ReviewsView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'TEACHER' || auth.user?.role === 'ADMINISTRATOR') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/student/exams/:id',
      name: 'student-exam-detail',
      component: StudentExamView
    },
    {
      path: '/subjects',
      name: 'subjects',
      component: SubjectView,
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        
        if (auth.user?.role === 'ADMINISTRATOR') {
          next()
        } else {
          next('/') 
        }
      }
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: StatisticsView,
      meta: { permission: 'STATISTICS_READ' }
    }
  ]
})

// Auth guard: /login is public; everything else needs a valid session, and a
// route may additionally require a permission via meta.permission.
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'login') {
      return { name: 'home' }
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  const required = to.meta.permission as string | undefined
  if (required && !auth.hasPermission(required)) {
    return { name: 'home' }
  }

  return true
})

export default router
