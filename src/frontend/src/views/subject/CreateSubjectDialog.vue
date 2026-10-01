<template>
  <v-dialog v-model="dialog" max-width="400">
    <template v-slot:activator="{ props: activatorProps }">
        <v-btn
          class="text-none font-weight-regular mb-2"
          prepend-icon="mdi-plus"
          text="Adicionar Disciplina"
          v-bind="activatorProps"
          color="primary"
        ></v-btn>
    </template>
    
    <v-card prepend-icon="mdi-book-plus" title="Nova Disciplina">
      <v-card-text>
        <v-text-field label="Nome da Disciplina*" required v-model="subject.name"></v-text-field>
        <v-text-field label="Código (Ex: MAT)*" required v-model="subject.code"></v-text-field>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="dialog = false; subject = { name: '', code: '' }"></v-btn>
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

const subject = ref({ name: '', code: '' })

const saveSubject = async () => {
  try {
    await RemoteService.createSubject(subject.value)
    
    dialog.value = false
    subject.value = { name: '', code: '' }
    emit('subject-saved')
  } catch (error: any) {
    console.error("Erro ao guardar disciplina:", error)
    const msg = error.response?.data?.message || "Verifique se o código já existe."
    alert(`Não foi possível guardar: ${msg}`)
  }
}
</script>