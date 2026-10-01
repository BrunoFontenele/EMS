import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { useAppearanceStore } from '@/stores/appearance'
import { useAuthStore } from '@/stores/auth'
import EmsError from '../models/EmsError'
import type PersonDto from '@/models/PersonDto'
import type SchoolDto from '@/models/SchoolDto'
import type SubjectDto from '@/models/SubjectDto'
import type AuthUser from '@/models/AuthUser'
import type CorrectionDto from '@/models/CorrectionDto'
import type ReviewDto from '@/models/ReviewDto'
import type SubmitReviewDto from '@/models/SubmitReviewDto'
import type StatisticsDto from '@/models/StatisticsDto'
import type ExamDto from '@/models/ExamDto'
import EmsError from '../models/EmsError'

const httpClient = axios.create()
httpClient.defaults.timeout = 50000
httpClient.defaults.baseURL = import.meta.env.VITE_ROOT_API

httpClient.interceptors.request.use((config) => {
  // Se os dados NÃO forem um FormData, definimos o cabeçalho como JSON
  if (config.data && !(config.data instanceof FormData)) {
    config.headers['Content-Type'] = 'application/json'
  }
  // Se for FormData, o navegador encarrega-se automaticamente de colocar 
  // o 'multipart/form-data' com o respetivo boundary.

  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }

  return config
}, (error) => {
  return Promise.reject(error)
})

export interface LoginResponse {
  token: string
  expiresInMs: number
  user: AuthUser
}

export default class RemoteServices {
  static async getAuditLogs(): Promise<any[]> {
    return httpClient.get('/audit')
  }

  static async getNotifications(): Promise<any[]> {
    return httpClient.get('/notifications')
  }

  static async markNotificationAsRead(id: number): Promise<void> {
    return httpClient.put(`/notifications/${id}/read`)
  }

  static async markAllNotificationsAsRead(): Promise<void> {
    return httpClient.put('/notifications/read-all')
  }

  // PEOPLE

  static async getPeople(): Promise<PersonDto[]> {
    return httpClient.get('/people')
  }

  static async createPerson(person: PersonDto): Promise<PersonDto> {
    return httpClient.post('/people', person)
  }

  static async updatePerson(id: number, person: PersonDto): Promise<PersonDto> {
    return httpClient.put(`/people/${id}`, person)
  }

  static async togglePersonActive(id: number) {
    return httpClient.put(`/people/${id}/toggle`)
  }
  
  /*
  static async deletePerson(id: number) {
    return httpClient.delete(`/people/${id}`)
  }
    */

  //SCHOOLS
  
  static async getSchools(): Promise<SchoolDto[]> {
    return httpClient.get('/schools')
  }

  static async createSchool(school: SchoolDto): Promise<SchoolDto> {
    return httpClient.post('/schools', school)
  }

  static async updateSchool(id: number, school: SchoolDto): Promise<SchoolDto> {
    return httpClient.put(`/schools/${id}`, school)
  }

  static async toggleSchoolActive(id: number): Promise<void> {
    return httpClient.put(`/schools/${id}/toggle`)
  }

  /*
  static async deactivateSchool(id: number): Promise<SchoolDto> {
    return httpClient.delete(`/schools/${id}`)
  }
    */

  //SUBJECTS

  static async getSubjects(): Promise<SubjectDto[]> {
    return httpClient.get('/subjects')
  }

  static async createSubject(subject: any): Promise<SubjectDto> {
    return httpClient.post('/subjects', subject)
  }

  static async updateSubject(id: number, subject: any): Promise<SubjectDto> {
    return httpClient.put(`/subjects/${id}`, subject)
  }

  static async toggleSubjectActive(id: number): Promise<void> {
    return httpClient.put(`/subjects/${id}/toggle`)
  }

  /*
  static async deleteSubject(id: number): Promise<SubjectDto> {
    return httpClient.delete(`/subjects/${id}`)
  }
    */

  //EXAMS
  static async getExam(id: number) {
    return httpClient.get(`/exams/${id}`)
  }

  static async getExams() {
    return httpClient.get('/exams')
  }

  static async createExam(formData: FormData) {
    return httpClient.post('/exams', formData)
  }

  static async updateExam(id: number, exam: any): Promise<any> {
    return httpClient.put(`/exams/${id}`, exam)
  }

  static async updateExamPdf(examId: number, file: File): Promise<ExamDto> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await httpClient.put(`/exams/${examId}/pdf`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
    return response.data
  }

  /*
  static async deleteExam(id: number): Promise<any> {
    return httpClient.delete(`/exams/${id}`)
  }
    */

  static async getExamPdf(id: number): Promise<Blob> {
    const response = await httpClient.get(`/exams/${id}/application`, { responseType: 'blob' })
    return response as any as Blob
  }

  static async releaseExam(examId: number) {
    return httpClient.patch(`/exams/${examId}/releaseExam`)
  }

  static async bulkReleaseExams(subjectCode: string) {
    return httpClient.patch(`/subjects/${subjectCode}/releaseAll`)
  }

  static async requestExamView(examId: number) {
    return httpClient.patch(`/exams/${examId}/request-view`)
  }

  static async getSchoolExamStatistics(subjectCode: string): Promise<StatisticsDto> {
    return httpClient.get(`/exams/statistics?subjectCode=${subjectCode}`)
  }

  //QUESTIONS
  static async getQuestions() {
    return httpClient.get('/questions')
  }

  static async getQuestionImage(questionId: number): Promise<Blob> {
    const response = await httpClient.get(`/questions/${questionId}/image`, {
      responseType: 'blob'
    })
    return response as any as Blob
  }

  static async deleteQuestion(id: number) {
    return httpClient.delete(`/questions/${id}`)
  }

  static async createQuestion(formData: FormData) {
    return httpClient.post('/questions', formData)
  }

  static async distributeQuestions(subjectCode: string) {
  return httpClient.post(`/exams/distribute?subjectCode=${subjectCode}`)
  }

  // STUDENT VIEW

  static async getStudentExamView() {
    return await httpClient.get(`/exams/student/me`)
  }

  static async getStudentExamDetails(examId: number) {
    return await httpClient.get(`/exams/${examId}/student-view`)
  }

  //CORRECTIONS

  static async getCorrections() {
    return await httpClient.get('/corrections')
  }
  
  static async updateCorrectionScore(correctionId: number, dto: CorrectionDto) {
    return await httpClient.patch(`/corrections/${correctionId}/score`, dto)
  }

  // REVIEWS

  static async getReviews() {
    return await httpClient.get('/reviews')
  }

  static async submitReviewRequest(examId: number, dto: SubmitReviewDto) {
    return await httpClient.post(`/reviews/${examId}`, dto)
  }
  
  static async submitReviewGrade(reviewId: number, dto: ReviewDto) {
    return await httpClient.patch(`/reviews/${reviewId}/grade`, dto)
  }

  // --- Authentication ---
  static async login(email: string, password: string): Promise<LoginResponse> {
    return httpClient.post('/auth/login', { email, password })
  }

  static async getCurrentUser(): Promise<AuthUser> {
    return httpClient.get('/auth/me')
  }

  static async impersonate(personId: number): Promise<LoginResponse> {
    return httpClient.post(`/auth/impersonate/${personId}`)
  }

  static async stopImpersonation(): Promise<LoginResponse> {
    return httpClient.post('/auth/impersonate/stop')
  }

  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to the server'
    } else if (error.message.split(' ')[0] === 'timeout') {
      return 'Request timeout - Server took too long to respond'
    } else {
      return error.response?.data?.message ?? 'Unknown Error'
    }
  }

  static async handleError(error: any): Promise<never> {
    // An expired/invalid token yields 401: drop the session so the router
    // guard sends the user back to the login page.
    if (error.response?.status === 401) {
      useAuthStore().logout()
    }
    const emsErr = new EmsError(
      await RemoteServices.errorMessage(error),
      error.response?.data?.code ?? -1
    )
    const appearance = useAppearanceStore()
    appearance.pushError(emsErr)
    appearance.loading = false
    throw emsErr
  }
}

// Attach the JWT (if any) to every outgoing request.
httpClient.interceptors.request.use((request) => {
  const auth = useAuthStore()
  if (auth.token) {
    request.headers.Authorization = `Bearer ${auth.token}`
  }
  return request
}, RemoteServices.handleError)
httpClient.interceptors.response.use((response) => response.data, RemoteServices.handleError)
