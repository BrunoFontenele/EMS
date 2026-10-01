<template>
  <v-container>
    <div class="d-flex align-center mb-6">
      <h2>Os Meus Exames</h2>
      <v-spacer></v-spacer>
      <v-btn color="primary" variant="tonal" prepend-icon="mdi-refresh" :loading="loading" @click="loadStudentExams">
        Atualizar
      </v-btn>
    </div>
    
    <v-row v-if="loading">
      <v-col class="text-center py-10" cols="12">
        <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
      </v-col>
    </v-row>

    <v-row v-else-if="exams.length === 0">
      <v-col cols="12">
        <v-alert type="info" variant="tonal" icon="mdi-information-outline">
          Ainda não tens exames com notas atribuídas.
        </v-alert>
      </v-col>
    </v-row>

    <v-row v-else>
      <v-col v-for="exam in exams" :key="exam.id" cols="12" sm="6" md="4">
        <v-card 
          elevation="2" 
          :class="['h-100 d-flex flex-column rounded-lg', isExamReleased(exam) ? 'border-primary border' : '']"
        >
          <v-card-item :class="isExamReleased(exam) ? 'bg-primary-lighten-5' : 'bg-grey-lighten-4'">
            <template v-slot:title>
              <div class="d-flex align-center">
                <span class="font-weight-bold">{{ exam.subjectCode }}</span>
                <v-icon v-if="isExamReleased(exam)" color="success" size="small" class="ml-2">mdi-check-decagram</v-icon>
              </div>
            </template>
            <template v-slot:subtitle>
              <span>{{ isExamReleased(exam) ? `Disponibilizado em: ${formatarData(exam.releaseDate)}` : 'Aguardando disponibilização' }}</span>
            </template>
            <template v-slot:append>
              <v-chip 
                size="small" 
                :color="isExamReleased(exam) ? 'success' : (exam.accessStatus === 'PENDING' ? 'warning' : 'grey')" 
                class="font-weight-medium"
              >
                {{ isExamReleased(exam) ? 'Disponibilizado' : (exam.accessStatus === 'PENDING' ? 'Pendente' : 'Bloqueado') }}
              </v-chip>
            </template>
          </v-card-item>

          <v-card-text class="flex-grow-1 text-center py-6">
            <div class="text-caption text-grey-darken-1 mb-1">Nota Final Obtida</div>
            <div :class="['text-h3 font-weight-bold', isExamReleased(exam) ? 'text-success' : 'text-primary']">
              {{ exam.finalScore ?? '-' }} <span class="text-h6 text-grey">pts</span>
            </div>
            <div v-if="isExamReleased(exam)" class="text-caption text-success mt-1">
              Prova e cotações prontas para consulta
            </div>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions class="pa-3">
            <v-btn 
              v-if="isExamReleased(exam)" 
              color="primary" 
              variant="flat" 
              block 
              prepend-icon="mdi-file-document-check-outline"
              @click="$router.push(`/student/exams/${exam.id}`)"
            >
              Consultar Prova e Notas
            </v-btn>

            <v-btn 
              v-else-if="exam.accessStatus === 'PENDING'" 
              color="warning" 
              variant="tonal" 
              block 
              disabled
              prepend-icon="mdi-clock-outline"
            >
              Aguardando Aprovação do Funcionário
            </v-btn>

            <v-btn 
              v-else 
              color="primary" 
              variant="outlined" 
              block 
              prepend-icon="mdi-lock-open-outline"
              :loading="requestingId === exam.id"
              @click="requestAccess(exam.id)"
            >
              Pedir Acesso para Ver a Prova
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3500">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemoteService from '@/services/RemoteService'

const exams = ref<any[]>([])
const loading = ref(true)
const requestingId = ref<number | null>(null)
const snackbar = ref({ show: false, text: '', color: 'success' })

onMounted(async () => {
  await loadStudentExams()
})

const loadStudentExams = async () => {
  loading.value = true
  try { 
    exams.value = await RemoteService.getExams()
  } catch (error) {
    console.error('Erro ao carregar exames do aluno:', error)
    snackbar.value = { show: true, text: 'Erro ao obter lista de exames.', color: 'error' }
  } finally {
    loading.value = false
  }
}

const isExamReleased = (exam: any) => {
  return exam.status === 'RELEASED' || exam.accessStatus === 'APPROVED' || !!exam.releaseDate
}

const requestAccess = async (examId: number) => {
  requestingId.value = examId
  try {
    await RemoteService.requestExamView(examId)
    snackbar.value = { 
      show: true, 
      text: 'Pedido de visualização enviado com sucesso aos funcionários!', 
      color: 'success' 
    }
    await loadStudentExams()
  } catch (error) {
    console.error('Erro ao requisitar acesso ao exame:', error)
    snackbar.value = { show: true, text: 'Erro ao submeter pedido de visualização.', color: 'error' }
  } finally {
    requestingId.value = null
  }
}

const formatarData = (dataString: string) => {
  if (!dataString) return '-'
  return new Date(dataString).toLocaleString('pt-PT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).replace(',', ' às') 
}
</script>

<style scoped>
.border-primary {
  border-color: rgb(var(--v-theme-primary)) !important;
}
.bg-primary-lighten-5 {
  background-color: rgba(var(--v-theme-primary), 0.06);
}
</style>