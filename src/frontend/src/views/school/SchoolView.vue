<template>
  <v-row align="center" class="mb-2">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Escolas</h2>
    </v-col>
    <v-col cols="auto">
      <v-btn
        color="primary"
        prepend-icon="mdi-plus"
        class="text-none font-weight-regular me-2"
        @click="createDialogRef?.open()"
      >
        Adicionar Escola
      </v-btn>
      <CreateSchoolDialog ref="createDialogRef" @school-saved="getSchools" />
      <EditSchoolDialog ref="editDialogRef" @school-saved="getSchools" />
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
    :items="schools"
    :search="search"
    :loading="loading"
    :custom-filter="customFilter"
    item-key="id"
    class="text-left"
    no-data-text="Sem escolas a apresentar."
  >
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

    <template #item.actions="{ item }">
      <v-icon @click="editSchool(item)" class="mr-2" title="Editar">
        mdi-pencil
      </v-icon>

      <v-icon 
        @click="toggleActiveSchool(item)" 
        class="mr-2" 
        :color="(item.active === true || String(item.active) === 'true') ? 'warning' : 'success'"
        :title="(item.active === true || String(item.active) === 'true') ? 'Desativar' : 'Ativar'"
      >
        {{ (item.active === true || String(item.active) === 'true') ? 'mdi-domain-off' : 'mdi-domain-plus' }}
      </v-icon>
    </template>
  </v-data-table>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type SchoolDto from '@/models/SchoolDto'
import RemoteService from '@/services/RemoteService'
import CreateSchoolDialog from './CreateSchoolDialog.vue'
import EditSchoolDialog from './EditSchoolDialog.vue'

const search = ref('')
const loading = ref(true)

const headers = [
  { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
  { title: 'Nome', key: 'name', value: 'name', sortable: true, filterable: true },
  { title: 'Código', key: 'code', value: 'code', sortable: true, filterable: true },
  { title: 'Região', key: 'region', value: 'region', sortable: true, filterable: true },
  { title: 'Estado', key: 'active', value: 'active', sortable: true, filterable: true },
  { title: 'Ações', key: 'actions', value: 'actions', sortable: false, filterable: false }
]

const schools: SchoolDto[] = reactive([])
const createDialogRef = ref()
const editDialogRef = ref()

getSchools()

async function getSchools() {
  loading.value = true
  schools.splice(0, schools.length)
  schools.push(...(await RemoteService.getSchools()))
  loading.value = false
}

const editSchool = (school: SchoolDto) => {
  editDialogRef.value.open(school)
}

const toggleActiveSchool = async (school: SchoolDto) => {
  if (!school.id) return

  const isActive = school.active === true || String(school.active) === 'true'
  const actionText = isActive ? 'desativar' : 'ativar'

  if (confirm(`Tem a certeza que deseja ${actionText} a escola ${school.name}?`)) {
    try {
      await RemoteService.toggleSchoolActive(school.id)
      await getSchools()
    } catch (error) {
      console.error(`Erro ao ${actionText} escola:`, error)
      alert(`Não foi possível ${actionText} a escola.`)
    }
  }
}

const customFilter = (value: any, query: string) => {
  if (value == null || !query) return false
  return String(value).toLowerCase().includes(query.trim().toLowerCase())
}
</script>