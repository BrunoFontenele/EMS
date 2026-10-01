export default interface CorrectionDto {
  id: number;
  questionId: number;
  professorEmail: string;
  score: number | null; // Pode ser null se ainda não tiver sido avaliada
  maxScore: number;
}