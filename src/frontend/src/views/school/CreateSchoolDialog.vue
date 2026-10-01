<template>
  <v-dialog v-model="dialog" max-width="400" persistent>
    <v-card prepend-icon="mdi-domain-plus" title="Nova Escola">
      <v-card-text>
        <v-text-field 
          label="Nome da Escola*" 
          required 
          v-model="school.name"
        ></v-text-field>
        <v-text-field 
          label="Código (Ex: IST)*" 
          required 
          v-model="school.code"
        ></v-text-field>
        <v-text-field 
          label="Região*" 
          required 
          v-model="school.region"
        ></v-text-field>
      </v-card-text>
      
      <v-divider></v-divider>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn 
          text="Cancelar" 
          variant="plain" 
          @click="closeDialog"
        ></v-btn>
        <v-btn 
          color="primary" 
          text="Guardar" 
          variant="tonal" 
          :loading="saving"
          @click="saveSchool"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const saving = ref(false)
const emit = defineEmits(['school-saved'])

const initialSchoolState = () => ({
  name: '',
  code: '',
  region: '',
  active: true
})

const school = ref(initialSchoolState())

const open = () => {
  school.value = initialSchoolState()
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
  school.value = initialSchoolState()
}

defineExpose({ open })

const saveSchool = async () => {
  if (!school.value.name?.trim() || !school.value.code?.trim() || !school.value.region?.trim()) {
    alert('Por favor, preencha todos os campos obrigatórios (*).')
    return
  }

  saving.value = true
  try {
    const payload = {
      name: school.value.name.trim(),
      code: school.value.code.trim().toUpperCase(),
      region: school.value.region.trim(),
      active: true
    }

    await RemoteService.createSchool(payload)
    
    closeDialog()
    emit('school-saved')
  } catch (error: any) {
    console.error('Erro ao guardar escola:', error)
    const backendMsg = error.response?.data?.message || error.response?.data?.detail || 'Verifique se o código da escola já existe ou se tem permissões.'
    alert(`Não foi possível guardar a escola: ${backendMsg}`)
  } finally {
    saving.value = false
  }
}
</script>