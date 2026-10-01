<template>
  <v-dialog v-model="dialog" max-width="400">
    <v-card prepend-icon="mdi-pencil" title="Editar Disciplina">
      <v-card-text>
        <v-text-field label="Nome da Disciplina*" required v-model="subject.name"></v-text-field>
        <v-text-field label="Código*" required v-model="subject.code"></v-text-field>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="dialog = false"></v-btn>
        <v-btn color="primary" text="Guardar" variant="tonal" @click="saveSubject"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const emit = defineEmits(['subject-saved'])

const subject = ref({ id: null, name: '', code: '', active: true })

const open = (subjectToEdit: any) => {
  const rawData = subjectToEdit.raw ? subjectToEdit.raw : subjectToEdit
  subject.value = { ...rawData }
  dialog.value = true
}

defineExpose({ open })

const saveSubject = async () => {
  try {
    if (subject.value.id) {
      await RemoteService.updateSubject(subject.value.id, subject.value)
    }
    dialog.value = false
    emit('subject-saved')
  } catch (error: any) {
    console.error("Erro ao atualizar disciplina:", error)
    alert("Não foi possível atualizar a disciplina.")
  }
}
</script>