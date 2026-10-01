package com.brunofontenele.ems.exceptions;

public enum ErrorMessage {

	NO_SUCH_PERSON("Não existe nenhuma pessoa com o ID %s", 1001),
	PERSON_NAME_NOT_VALID("O nome da pessoa especificado não é válido.", 1002),
	PERSON_ALREADY_EXISTS("Já existe uma pessoa com o ID %s", 1003),
	PERSON_EMAIL_NOT_VALID("O email especificado não é válido.", 1004),
	PERSON_PASSWORD_NOT_VALID("A palavra-passe especificada não é válida.", 1005),
	EMAIL_ALREADY_EXISTS("Já existe uma conta com o email %s", 1006),
	NO_AVAILABLE_TEACHER("Não existem professores disponívei", 1007),
	INACTIVE_PERSON("A pessoa com o email %s encontra-se inativa.", 1008),

	INVALID_CREDENTIALS("Email ou palavra-passe inválidos.", 2001),
	CANNOT_IMPERSONATE_SELF("Não é possível personificar a própria conta.", 2002),
	NOT_AUTHENTICATED("Não autenticado.", 2003),
	NOT_IMPERSONATING("Não está a personificar nenhuma conta.", 2004),
	ACCESS_DENIED("Não tem permissões para executar esta ação.", 2005),

	NO_SUCH_SCHOOL("Não existe nenhuma escola com o ID %s", 3001),
	SCHOOL_NAME_NOT_VALID("O nome da escola especificado não é válido.", 3002),
	SCHOOL_ALREADY_EXISTS("Já existe uma escola com o ID %s", 3003),
	SCHOOL_CODE_NOT_VALID("O código da escola especificado não é válido.", 3004),
	SCHOOL_REGION_NOT_VALID("A região da escola especificada não é válida.", 3005),
	INACTIVE_SCHOOL("A escola com o código %s encontra-se inativa.", 3006),

	NO_SUCH_SUBJECT("Não existe nenhuma disciplina com o código %s", 4001),
	SUBJECT_NAME_NOT_VALID("O nome da disciplina especificado não é válido.", 4002),
	SUBJECT_ALREADY_EXISTS("Já existe uma disciplina com o código %s", 4003),
	SUBJECT_CODE_NOT_VALID("O código da disciplina especificado não é válido.", 4004),
	INACTIVE_SUBJECT("A disciplina com o código %s encontra-se inativa.", 4005),

	NO_SUCH_EXAM("Não existe nenhum exame com o ID %s", 5001),
	EXAM_FILE_PATH_NOT_VALID("O caminho do ficheiro do exame especificado não é válido.", 5002),
	EXAM_ALREADY_EXISTS("Já existe um exame com o ID %s", 5003),
	EXAM_UPLOAD_CLOSED("O upload do exame com o ID %s já foi fechado.", 5004),
	NO_EXAMS_TO_DISTRIBUTE("Não existem exames para distribuir na disciplina com o código %s.", 5005),
	NO_PROFESSORS_ASSIGNED_TO_SUBJECT("Não existem professores atribuídos à disciplina com o código %s.", 5006),
	EXAM_VIEWING_NOT_ALLOWED("Não é permitido visualizar o exame com o ID %s.", 5007),
	EXAM_APPROVAL_NOT_ALLOWED("Não é permitido aprovar o exame com o ID %s.", 5008),
	PENDING_EXAMS_EXIST("Existem exames pendentes de aprovação na disciplina com o código %s.", 5009),
	NO_CLOSED_EXAMS("Não existem exames fechados na disciplina com o código %s.", 5010),
	EXAM_NOT_RELEASED("O exame com o ID %s ainda não se encontra disponível para consulta.", 5011),

	NO_SUCH_QUESTION("Não existe nenhuma questão com o ID %s", 6001),
	QUESTION_ALREADY_EXISTS("Já existe uma questão com o ID %s", 6002),
	QUESTION_FILE_PATH_NOT_VALID("O caminho do ficheiro da questão especificado não é válido.", 6003),
	QUESTION_NUMBER_NOT_VALID("O número da questão especificado não é válido.", 6004),
	QUESTION_MAX_SCORE_NOT_VALID("A pontuação máxima da questão especificada não é válida.", 6005),
	QUESTION_NOT_ASSIGNED_TO_EXAM("A questão com o ID %s não está atribuída ao exame com o ID %s.", 6006),
	NO_QUESTIONS_TO_DISTRIBUTE("Não existem questões para distribuir no exame com o ID %s.", 6007),

	INVALID_FILE_TYPE("O tipo de ficheiro especificado não é válido.", 7001),
	FAILED_TO_STORE_FILE("Falha ao armazenar o ficheiro.", 7002),
	FAILED_TO_DELETE_FILE("Falha ao eliminar o ficheiro.", 7003),
	FAILED_TO_READ_FILE("Falha ao ler o ficheiro.", 7004),
	
	NO_SUCH_CORRECTION("Não existe nenhuma correção com o ID %s", 8001),
	PROFESSOR_NOT_ASSIGNED_TO_QUESTION("O professor com o email %s não está atribuído à questão com o ID %s", 8002),
	UNAUTHORIZED_ACCESS("Não tens permissão para avaliar o fragmento %s.", 8003),
	SCORE_EXCEEDS_MAX("A nota inserida %s é superior à cotação máxima da pergunta %s.", 8004),

	NO_SUCH_REVIEW("Não existe nenhuma revisão com o ID %s", 9001),
	NO_SUCH_REVIEW_ITEM("Não existe nenhum pedido de revisão com o ID %s", 9002),
	REVIEW_DEADLINE_PAST("O prazo para submissão de pedidos de revisão do exame com o ID %s já passou.", 9003),
	REVIEW_ALREADY_EXISTS("Já existe um pedido de revisão para a questão com o ID %s", 9004),
	NOT_IN_REVIEW("O pedido de revisão com o ID %s não se encontra em estado de revisão.", 9005),
	INVALID_SCORE("A pontuação especificada %s não é válida.", 9006)
	;

	private final String label;
	private final int code;

	ErrorMessage(String label, int code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public int getCode() {
		return this.code;
	}
}
