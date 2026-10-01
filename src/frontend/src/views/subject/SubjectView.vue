<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Disciplinas</h2>
    </v-col>
    <v-col cols="auto">
      <CreateSubjectDialog @subject-saved="getSubjects" />
      <EditSubjectDialog ref="editDialogRef" @subject-saved="getSubjects" />
    </v-col>
  </v-row>
  
  <v-text-field
    v-model="search"
    label="Pesquisar"
    prepend-inner-icon="mdi-magnify"
    variant="outlined"
    hide-details
    single-line
    class="mb-4"
  ></v-text-field>

  <v-data-table
    :headers="headers"
    :items="subjects"
    :search="search"
    :loading="loading"
    :custom-filter="customFilter"
    item-key="id"
    class="text-left"
    no-data-text="Sem disciplinas a apresentar."
  >
    <!-- Estado (Ativo/Inativo) -->
    <template #item.active="{ item }">
      <v-chip 
        v-if="item.active === true || String(item.active) === 'true'" 
        color="green" 
        size="small" 
        variant="flat"
      >
        Ativo
      </v-chip>
      <v-chip v-else color="grey" size="small" variant="flat">
        Inativo
      </v-chip>
    </template>

    <!-- Ações (Editar, Toggle Ativar/Desativar e Eliminar) -->
    <template #item.actions="{ item }">
      <!-- Editar -->
      <v-icon @click="editSubject(item)" class="mr-2" title="Editar">
        mdi-pencil
      </v-icon>

      <!-- Alternar Estado (Ativar / Desativar) -->
      <v-icon 
        @click="toggleActiveSubject(item)" 
        class="mr-2" 
        :color="(item.active === true || String(item.active) === 'true') ? 'warning' : 'success'"
        :title="(item.active === true || String(item.active) === 'true') ? 'Desativar' : 'Ativar'"
      >
        {{ (item.active === true || String(item.active) === 'true') ? 'mdi-book-off' : 'mdi-book-plus' }}
      </v-icon>

    </template>
  </v-data-table>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type SubjectDto from '@/models/SubjectDto'
import RemoteService from '@/services/RemoteService'
import CreateSubjectDialog from './CreateSubjectDialog.vue'
import EditSubjectDialog from './EditSubjectDialog.vue'

const search = ref('')
const loading = ref(true)

const headers = [
  { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
  { title: 'Nome', key: 'name', value: 'name', sortable: true, filterable: true },
  { title: 'Código', key: 'code', value: 'code', sortable: true, filterable: true },
  { title: 'Estado', key: 'active', value: 'active', sortable: true, filterable: true },
  { title: 'Ações', key: 'actions', value: 'actions', sortable: false, filterable: false }
]

const subjects: SubjectDto[] = reactive([])
const editDialogRef = ref()

getSubjects()

async function getSubjects() {
  loading.value = true
  subjects.splice(0, subjects.length)
  subjects.push(...(await RemoteService.getSubjects())) 
  loading.value = false
}

const editSubject = (subject: SubjectDto) => {
  editDialogRef.value.open(subject)
}

const toggleActiveSubject = async (subject: SubjectDto) => {
  if (!subject.id) return

  const isActive = subject.active === true || String(subject.active) === 'true'
  const actionText = isActive ? 'desativar' : 'ativar'

  if (confirm(`Tem a certeza que deseja ${actionText} a disciplina ${subject.name}?`)) {
    try {
      // Chama o método PATCH de alternância
      await RemoteService.toggleSubjectActive(subject.id)
      await getSubjects()
    } catch (error: any) {
      console.error(`Erro ao ${actionText} disciplina:`, error)
      const msg = error.response?.data?.message || `Não foi possível ${actionText} a disciplina.`
      alert(`Erro:\n${msg}`)
    }
  }
}

/*
// Eliminar permanentemente
const deleteSubject = async (subject: SubjectDto) => {
  if (!subject.id) return

  if (confirm(`ATENÇÃO: Tem a certeza que deseja ELIMINAR permanentemente a disciplina ${subject.name}? Esta ação não pode ser revertida.`)) {
    try {
      await RemoteService.deleteSubject(subject.id)
      await getSubjects()
    } catch (error: any) {
      console.error('Erro ao eliminar disciplina:', error)
      const msg = error.response?.data?.message || 'Não foi possível eliminar a disciplina.'
      alert(`Erro:\n${msg}`)
    }
  }
}
  */
const customFilter = (value: any, query: string) => {
  if (value == null || !query) return false
  return String(value).toLowerCase().includes(query.trim().toLowerCase())
}
</script>