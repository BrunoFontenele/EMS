export default interface ExamDto {
  id?: number;
  filePath?: string;
  studentEmail?: string;
  schoolCode?: string;
  subjectCode?: string;
  finalScore?: number; 
  viewRequested?: boolean; 
  releaseDate?: string;    
  status?: 'UPLOAD_IN_PROGRESS' | 'IN_REVIEW' | 'CLOSED' | 'RELEASED'; 
}