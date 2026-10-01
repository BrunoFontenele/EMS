<template>
  <v-dialog v-model="dialog" max-width="500">
    <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular mb-2"
          prepend-icon="mdi-plus"
          text="Adicionar Exame"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
    </template>
    
    <v-card prepend-icon="mdi-file-document-plus" title="Novo Exame">
      <v-card-text>
        <v-autocomplete 
          :items="students" 
          item-title="name" 
          item-value="email" 
          label="Aluno*" 
          v-model="exam.studentEmail">
        </v-autocomplete>

        <!-- Escola: item-value passa a "code" e v-model passa a "exam.schoolCode" -->
        <v-autocomplete 
          :items="schools" 
          item-title="name" 
          item-value="code" 
          label="Escola*" 
          v-model="exam.schoolCode">
        </v-autocomplete>

        <!-- Disciplina: item-value passa a "code" e v-model passa a "exam.subjectCode" -->
        <v-autocomplete 
          :items="subjects" 
          item-title="name" 
          item-value="code" 
          label="Disciplina*" 
          v-model="exam.subjectCode">
        </v-autocomplete>
        <!-- O campo Data de Lançamento foi removido daqui! -->

        <v-file-input
          v-model="examFile"
          accept="application/pdf"
          label="Ficheiro do Exame (PDF)*"
          prepend-icon="mdi-pdf-box"
          show-size
        ></v-file-input>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="close"></v-btn>
        <v-btn color="primary" text="Guardar" variant="tonal" @click="saveExam" :disabled="!examFile"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const emit = defineEmits(['exam-saved'])

const students = ref<any[]>([])
const schools = ref<any[]>([])
const subjects = ref<any[]>([])

// Removido o releaseDate daqui também
const exam = ref({ 
  studentEmail: '', 
  schoolCode: '', 
  subjectCode: '' 
})
const examFile = ref<File | null>(null)

const close = () => {
  dialog.value = false
  // 2. Garante que no close também voltas a usar os nomes corretos
  exam.value = { studentEmail: '', schoolCode: '', subjectCode: '' }
  examFile.value = null
}

const saveExam = async () => {
  // 3. VAMOS INSPECIONAR O QUE ESTÁ A SER ENVIADO!
  console.log("DADOS A ENVIAR:", JSON.stringify(exam.value))

  try {
    const formData = new FormData()
    formData.append('exam', new Blob([JSON.stringify(exam.value)], { type: 'application/json' }))
    
    if (examFile.value) {
      formData.append('file', examFile.value)
    }

    await RemoteService.createExam(formData)
    close()
    emit('exam-saved')
  } catch (error: any) {
    console.error("Erro ao guardar exame:", error)
    alert("Não foi possível guardar o exame.")
  }
}

watch(dialog, async (isOpen) => {
  if (isOpen) {
    const people = await RemoteService.getPeople()
    students.value = people.filter((p: any) => p.role === 'STUDENT' || p.type === 'STUDENT')
    
    const allSchools = await RemoteService.getSchools()
    schools.value = allSchools.filter((s: any) => s.active === true || String(s.active) === 'true')
    
    const allSubjects = await RemoteService.getSubjects()
    subjects.value = allSubjects.filter((sub: any) => sub.active === true || String(sub.active) === 'true')
  }
})
</script>