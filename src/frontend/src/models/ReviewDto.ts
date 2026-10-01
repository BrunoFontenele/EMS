export enum ReviewStatus {
  PENDING = 'PENDING',
  IN_REVIEW = 'IN_REVIEW',
  REVIEWED = 'REVIEWED'
}

export interface ReviewDto {
  id: number | null
  questionId: number
  studentJustification: string
  oldScore: number | null
  newScore: number | null
  createdAt: string // String ISO (ex: "2026-08-29T23:58:00")
  reviewStatus: ReviewStatus
  assignedTeacherName: string | null
  assignedTeacherEmail: string | null
}