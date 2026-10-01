import type StudentResultDto from '@/models/StudentsResultsDto'

export default interface SchoolSubjectStatisticsDto {
  schoolName: string
  schoolCode: string
  subjectCode: string
  subjectName: string
  totalStudents: number
  averageScore: number
  highestScore: number | null
  lowestScore: number | null
  approvedCount: number
  failedCount: number
  approvalRate: number
  studentResults: StudentResultDto[]
}