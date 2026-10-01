<template>
  <v-dialog v-model="dialog" max-width="400">
    <v-card prepend-icon="mdi-pencil" title="Editar Escola">
      <v-card-text>
        <v-text-field label="Nome da Escola*" required v-model="school.name"></v-text-field>
        
        <v-text-field label="Código*" required v-model="school.code"></v-text-field>
        
        <v-text-field label="Região*" required v-model="school.region"></v-text-field>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="dialog = false"></v-btn>
        <v-btn color="primary" text="Guardar" variant="tonal" @click="saveSchool"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const emit = defineEmits(['school-saved'])

const school = ref({ id: undefined, name: '', code: '', region: '', active: true })

const open = (schoolToEdit: any) => {
  const rawData = schoolToEdit.raw ? schoolToEdit.raw : schoolToEdit
  school.value = { ...rawData }
  dialog.value = true
}

defineExpose({ open })

const saveSchool = async () => {
  try {
    if (school.value.id) {
      await RemoteService.updateSchool(school.value.id, school.value)
    }
    dialog.value = false
    emit('school-saved')
  } catch (error: any) {
    console.error("Erro ao atualizar escola:", error)
    alert("Não foi possível atualizar a escola.")
  }
}
</script>