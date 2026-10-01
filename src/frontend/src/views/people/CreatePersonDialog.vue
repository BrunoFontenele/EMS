<template>
  <v-dialog v-model="dialog" max-width="400">
    <template v-slot:activator="{ props: activatorProps }">
      <v-btn
        class="text-none font-weight-regular mb-2"
        prepend-icon="mdi-plus"
        text="Adicionar Pessoa"
        v-bind="activatorProps"
        color="primary"
      ></v-btn>
    </template>
    
    <v-card 
      prepend-icon="mdi-account-plus" 
      title="Nova Pessoa"
    >
      <v-card-text>
        <v-text-field label="Nome*" required v-model="person.name"></v-text-field>
        <v-text-field label="Email*" type="email" required v-model="person.email"></v-text-field>
        
        <v-text-field
          label="Palavra-passe* (Mínimo 8 caracteres)"
          type="password"
          required
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
          v-if="person.type === 'Professor' || person.type === 'Aluno'"
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
        <v-btn text="Cancelar" variant="plain" @click="dialog = false; resetForm()"></v-btn>
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

const person = ref<PersonDto>({
  name: '', email: '', password: '', type: ''
})

const resetForm = () => {
  person.value = { name: '', email: '', password: '', type: '' }
}

const savePerson = async () => {
  const dataToSave = { ...person.value }
  dataToSave.type = typeToEnum[dataToSave.type as keyof typeof typeToEnum] || dataToSave.type

  try {
    await RemoteService.createPerson(dataToSave)
    
    dialog.value = false
    resetForm()
    
    emit('person-saved') 
  } catch (error) {
    console.error("Erro ao guardar pessoa:", error)
    alert("Não foi possível guardar. Verifique se o email já está em uso ou se faltam dados obrigatórios.")
  }
}

watch(() => person.value.type, (newType) => {
  if (newType !== 'Professor' && newType !== 'Aluno') {
    person.value.subjectCode = undefined
    if (newType !== 'Funcionário') {
      person.value.schoolCode = undefined
    }
  }
})

watch(dialog, async (isOpen) => {
  if (isOpen) {
    try {
      const allSchools = await RemoteService.getSchools()
      const allSubjects = await RemoteService.getSubjects()
      
      schools.value = allSchools.filter((school: any) => 
        school.active === true || String(school.active) === 'true'
      )

      subjects.value = allSubjects.filter((subject: any) => 
        subject.active === true || String(subject.active) === 'true'
      )
    } catch (error) {
      console.error("Erro ao carregar escolas e disciplinas:", error)
    }
  }
})
</script>