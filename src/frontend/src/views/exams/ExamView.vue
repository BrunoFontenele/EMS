<template>
  <v-row align="center" class="mb-4">
    <v-col>
      <h2 class="text-left ml-1">Gestão de Exames</h2>
    </v-col>
    <v-col cols="auto" class="d-flex gap-2">
      <v-btn 
        color="success" 
        prepend-icon="mdi-check-all" 
        @click="bulkReleaseDialog = true"
        class="mr-2"
      >
        Disponibilizar em Lote
      </v-btn>
      <CreateExamDialog @exam-saved="getExams" />
      <EditExamDialog ref="editDialogRef" @exam-saved="getExams" />
    </v-col>
  </v-row>
  
  <v-text-field
    v-model="search"
    label="Pesquisar Exame"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    hide-details
    single-line
    class="mb-4"
  ></v-text-field>

  <v-data-table
    :headers="headers"
    :items="exams"
    :search="search"
    :loading="loading"
    :custom-filter="customFilter"
    item-key="id"
    class="text-left"
    no-data-text="Sem exames a apresentar."
  >
    <template #item.status="{ item }">
      <v-chip v-if="item.status === 'UPLOAD_IN_PROGRESS'" color="orange" size="small" variant="flat">Upload em Curso</v-chip>
      <v-chip v-else-if="item.status === 'IN_REVIEW'" color="blue" size="small" variant="flat">Em Revisão</v-chip>
      <v-chip v-else-if="item.status === 'CLOSED'" color="purple" size="small" variant="flat">Corrigido</v-chip>
      <v-chip v-else color="green" size="small" variant="flat">Lançado</v-chip>
    </template>

    <template #item.viewRequested="{ item }">
      <v-icon v-if="item.viewRequested" color="warning">mdi-check-circle</v-icon>
      <span v-else>-</span>
    </template>

    <template #item.releaseDate="{ item }">
      {{ formatarData(item.releaseDate) }}
    </template>

    <template #item.actions="{ item }">
      <v-icon 
        v-if="canReleaseExam(item)" 
        @click="releaseSingleExam(item)" 
        class="mr-2" 
        title="Disponibilizar Prova ao Aluno" 
        color="success"
      >
        mdi-eye-check
      </v-icon>

      <v-icon @click="viewPdf(item)" class="mr-2" title="Visualizar PDF" color="red">mdi-file-pdf-box</v-icon>
      
      <v-icon 
        v-if="authStore.user?.role === 'ADMINISTRATOR'" 
        @click="editExam(item)" 
        class="mr-2" 
        title="Editar Nota/Estado"
      >
        mdi-pencil
      </v-icon>

      <v-btn icon="mdi-scissors-cutting" variant="text" size="small" color="primary" @click="$router.push(`/exams/${item.id}/process`)"></v-btn>
    </template>
  </v-data-table>

  <v-dialog v-model="bulkReleaseDialog" max-width="400px">
    <v-card>
      <v-toolbar color="success" title="Disponibilizar Exames"></v-toolbar>
      <v-card-text class="pt-4">
        <p class="mb-4">Introduza a disciplina para disponibilizar todos os exames fechados aos alunos.</p>
        <v-text-field
          v-model="bulkSubjectCode"
          label="Código da Disciplina (ex: MAT-A)"
          variant="outlined"
          hide-details
        ></v-text-field>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="grey-darken-1" variant="text" @click="bulkReleaseDialog = false">Cancelar</v-btn>
        <v-btn color="success" variant="flat" @click="confirmBulkRelease">Confirmar</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import RemoteService from '@/services/RemoteService'
import CreateExamDialog from './CreateExamDialog.vue'
import EditExamDialog from './EditExamDialog.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

let search = ref('')
let loading = ref(true)

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Aluno (Email)', key: 'studentEmail' }, 
  { title: 'Disciplina', key: 'subjectCode' }, 
  { title: 'Nota', key: 'finalScore' },
  { title: 'Data Lançamento', key: 'releaseDate' },
  { title: 'Estado', key: 'status' },
  { title: 'Revisão Pedida', key: 'viewRequested' },
  { title: 'Ações', key: 'actions', sortable: false }
]

const exams: any[] = reactive([])
const editDialogRef = ref()
const bulkReleaseDialog = ref(false)
const bulkSubjectCode = ref('')

const canReleaseExam = (item: any) => {
  const targetExam = item.raw ? item.raw : item
  if (targetExam.status !== 'CLOSED') return false

  const isAdmin = authStore.user?.role === 'ADMINISTRATOR'
  return isAdmin || targetExam.viewRequested === true
}

const confirmBulkRelease = async () => {
  if (!bulkSubjectCode.value.trim()) return

  try {
    await RemoteService.bulkReleaseExams(bulkSubjectCode.value.trim())
    alert(`Exames da disciplina ${bulkSubjectCode.value} disponibilizados com sucesso!`)
    bulkReleaseDialog.value = false
    bulkSubjectCode.value = ''
    await getExams() 
  } catch (error) {
    console.error("Erro no bulk release:", error)
    alert("Erro ao disponibilizar exames. Verifique se existem exames fechados para esta disciplina.")
  }
}

const releaseSingleExam = async (item: any) => {
  const targetExam = item.raw ? item.raw : item
  if (confirm(`Tem a certeza que deseja disponibilizar o exame de ${targetExam.studentEmail}?`)) {
    try {
      await RemoteService.releaseExam(targetExam.id)
      alert("Exame disponibilizado com sucesso!")
      await getExams()
    } catch (error) {
      console.error("Erro ao disponibilizar exame:", error)
      alert("Erro ao disponibilizar o exame.")
    }
  }
}

const formatarData = (dataString: string) => {
  if (!dataString) return '-'
  const data = new Date(dataString)
  
  return data.toLocaleString('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(',', ' às') 
}

getExams()

async function getExams() {
  exams.splice(0, exams.length)
  exams.push(...(await RemoteService.getExams())) 
  loading.value = false
}

const editExam = (item: any) => {
  const examData = item.raw ? item.raw : item
  editDialogRef.value.open(examData)
}

const viewPdf = async (item: any) => {
  const targetExam = item.raw ? item.raw : item
  try {
    const blob = await RemoteService.getExamPdf(targetExam.id)
    const fileUrl = window.URL.createObjectURL(blob)
    window.open(fileUrl, '_blank')
  } catch (error) {
    console.error("Erro ao abrir PDF:", error)
    alert("Não foi possível transferir o PDF. Verifique se o ficheiro existe no servidor.")
  }
}

const customFilter = (value: any, query: string) => {
  if (value == null || !query) return false
  return String(value).toLowerCase().includes(query.trim().toLowerCase())
}
</script>