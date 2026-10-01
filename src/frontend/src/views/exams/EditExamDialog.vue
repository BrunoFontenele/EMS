<template>
  <v-dialog v-model="dialog" max-width="500" persistent>
    <v-card prepend-icon="mdi-pencil" title="Atualizar Exame">
      <v-card-text>
        <v-text-field 
          label="Email do Aluno*" 
          type="email" 
          required 
          v-model="exam.studentEmail"
        ></v-text-field>

        <v-autocomplete
          :items="schools"
          item-title="name"
          item-value="code"
          label="Escola"
          clearable
          v-model="exam.schoolCode"
        ></v-autocomplete>

        <v-autocomplete
          :items="subjects"
          item-title="name"
          item-value="code"
          label="Disciplina"
          clearable
          v-model="exam.subjectCode"
        ></v-autocomplete>

        <v-text-field 
          label="Nota Final" 
          type="number" 
          v-model="exam.finalScore"
        ></v-text-field>
        
        <v-select
          :items="statusOptions"
          item-title="label"
          item-value="value"
          label="Estado do Exame*"
          required
          v-model="exam.status"
        ></v-select>

        <v-file-input
          label="Substituir Ficheiro PDF (Opcional)"
          accept="application/pdf"
          prepend-icon="mdi-file-pdf-box"
          variant="outlined"
          density="compact"
          show-size
          v-model="pdfFile"
          class="mt-2"
        ></v-file-input>

        <v-switch
          v-model="exam.viewRequested"
          color="primary"
          label="Visualização Solicitada pelo Aluno?"
          hide-details
          class="mt-1"
        ></v-switch>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" :disabled="loading" @click="closeDialog"></v-btn>
        <v-btn 
          color="primary" 
          text="Guardar" 
          variant="tonal" 
          :loading="loading" 
          @click="saveExam"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const loading = ref(false)
const emit = defineEmits(['exam-saved'])

const schools = ref<any[]>([])
const subjects = ref<any[]>([])
const pdfFile = ref<File | null>(null)

const exam = ref<any>({
  id: null,
  studentEmail: '',
  schoolCode: null,
  subjectCode: null,
  finalScore: null,
  status: '',
  viewRequested: false
})

const statusOptions = [
  { label: 'Upload em Curso', value: 'UPLOAD_IN_PROGRESS' },
  { label: 'Em Revisão', value: 'IN_REVIEW' },
  { label: 'Fechado / Corrigido', value: 'CLOSED' },
  { label: 'Lançado', value: 'RELEASED' }
]

const open = (examToEdit: any) => {
  const rawData = examToEdit.raw ? examToEdit.raw : examToEdit
  exam.value = { ...rawData }
  pdfFile.value = null
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
  pdfFile.value = null
}

defineExpose({ open })

const saveExam = async () => {
  if (!exam.value.id) return

  loading.value = true
  try {
    // 1. Atualiza os dados cadastrais via JSON
    await RemoteService.updateExam(exam.value.id, exam.value)

    // 2. Se um novo PDF tiver sido selecionado, atualiza o ficheiro via Multipart
    if (pdfFile.value) {
      const fileToSend = Array.isArray(pdfFile.value) ? pdfFile.value[0] : pdfFile.value
      await RemoteService.updateExamPdf(exam.value.id, fileToSend)
    }

    closeDialog()
    emit('exam-saved')
  } catch (error: any) {
    console.error("Erro ao atualizar exame:", error)
    const msg = error.response?.data?.message || error.response?.data?.detail || "Erro ao processar as alterações."
    alert(`Não foi possível atualizar o exame: ${msg}`)
  } finally {
    loading.value = false
  }
}

watch(dialog, async (isOpen) => {
  if (isOpen) {
    try {
      const allSchools = await RemoteService.getSchools()
      schools.value = allSchools.filter((school: any) => 
        school.active === true || String(school.active) === 'true'
      )
      subjects.value = await RemoteService.getSubjects()
    } catch (error) {
      console.error("Erro ao carregar escolas e disciplinas:", error)
    }
  }
})
</script>