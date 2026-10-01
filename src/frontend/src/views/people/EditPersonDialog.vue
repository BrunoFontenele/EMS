<template>
  <v-dialog v-model="dialog" max-width="400">
    <v-card prepend-icon="mdi-pencil" title="Editar Pessoa">
      <v-card-text>
        <v-text-field label="Nome*" required v-model="person.name"></v-text-field>
        <v-text-field label="Email*" type="email" required v-model="person.email"></v-text-field>
        
        <v-text-field
          label="Nova Palavra-passe (Opcional, mínimo 8 caracteres)"
          type="password"
          v-model="person.password"
        ></v-text-field>

        <v-select
          :items="['Administrador', 'Funcionário', 'Professor', 'Aluno']"
          label="Categoria*"
          required
          v-model="person.type"
        ></v-select>

        <v-autocomplete
          v-if="person.type === 'Professor' || person.type === 'Funcionário' || person.type === 'Aluno'" 
          :items="schools"
          item-title="name"
          item-value="code"
          label="Escola"
          clearable
          v-model="person.schoolCode"
        ></v-autocomplete>

        <v-autocomplete
          v-if="person.type === 'Professor'"
          :items="subjects"
          item-title="name"
          item-value="code"
          label="Disciplina"
          clearable
          v-model="person.subjectCode"
        ></v-autocomplete>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Cancelar" variant="plain" @click="dialog = false"></v-btn>
        <v-btn color="primary" text="Guardar" variant="tonal" @click="savePerson"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type PersonDto from '@/models/PersonDto'
import RemoteService from '@/services/RemoteService'

const dialog = ref(false)
const emit = defineEmits(['person-saved'])

const schools = ref<any[]>([])
const subjects = ref<any[]>([])

const typeToEnum = {
  'Administrador': 'ADMINISTRATOR',
  'Funcionário': 'SCHOOL_STAFF',
  'Professor': 'TEACHER',
  'Aluno': 'STUDENT'
}
const enumToType = {
  'ADMINISTRATOR': 'Administrador',
  'SCHOOL_STAFF': 'Funcionário',
  'TEACHER': 'Professor',
  'STUDENT': 'Aluno'
}

const person = ref<any>({ name: '', email: '', password: '', type: '', active: true })

const open = (personToEdit: any) => {
  person.value = { ...personToEdit }
  
  if (person.value.type) {
    person.value.type = enumToType[person.value.type as keyof typeof enumToType] || person.value.type
  }
  
  if (person.value.active === undefined) {
    person.value.active = true
  }

  person.value.password = ''
  dialog.value = true
}

defineExpose({ open })

const savePerson = async () => {
  const dataToSave = { ...person.value }
  dataToSave.type = typeToEnum[dataToSave.type as keyof typeof typeToEnum] || dataToSave.type

  // Garante que o campo active é sempre true se não for explicitamente inativo
  dataToSave.active = dataToSave.active ?? true

  // Se a password estiver vazia, remove para evitar erro de validação (mínimo 8 caracteres)
  if (!dataToSave.password || dataToSave.password.trim() === '') {
    delete dataToSave.password
  }

  try {
    if (dataToSave.id) {
      await RemoteService.updatePerson(dataToSave.id, dataToSave)
    }
    dialog.value = false
    emit('person-saved')
  } catch (error) {
    console.error("Erro ao atualizar pessoa:", error)
  }
}

watch(() => person.value.type, (newType) => {
  if (newType !== 'Professor') {
    person.value.subjectCode = null
  }
  if (newType !== 'Professor' && newType !== 'Funcionário' && newType !== 'Aluno') {
    person.value.schoolCode = null
  }
})

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