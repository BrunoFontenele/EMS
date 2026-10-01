<template>
  <v-row align="center">
    <v-col>
      <h2 class="text-left ml-1">Listagem de Pessoas</h2>
    </v-col>
    <v-col cols="auto">
      <CreatePersonDialog @person-saved="getPeople" />
      <EditPersonDialog ref="editDialogRef" @person-saved="getPeople" />
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
    :items="people"
    :search="search"
    :loading="loading"
    :custom-filter="customFilter"
    item-key="id"
    class="text-left"
    no-data-text="Sem pessoas a apresentar."
  >
    <template #item.type="{ item }">
      <v-chip v-if="item.type === 'ADMINISTRATOR'" color="purple" size="small" variant="flat">
        Administrador
      </v-chip>
      <v-chip v-else-if="item.type === 'SCHOOL_STAFF'" color="orange" size="small" variant="flat">
        Funcionário
      </v-chip>
      <v-chip v-else-if="item.type === 'TEACHER'" color="blue" size="small" variant="flat">
        Professor
      </v-chip>
      <v-chip v-else color="brown" size="small" variant="flat">
        Aluno
      </v-chip>
    </template>

    <template #item.active="{ item }">
      <v-chip v-if="item.active" color="green" size="small" variant="flat">
        Ativo
      </v-chip>
      <v-chip v-else color="grey" size="small" variant="flat">
        Inativo
      </v-chip>
    </template>

    <template #item.actions="{ item }">
      <v-icon
        v-if="auth.hasPermission('PERSON_IMPERSONATE') && item.id !== auth.user?.id"
        @click="impersonate(item)"
        class="mr-2"
        color="primary"
        title="Personificar"
      >
        mdi-account-switch
      </v-icon>
      
      <v-icon @click="editPerson(item)" class="mr-2" title="Editar">
        mdi-pencil
      </v-icon>

      <v-icon 
        @click="toggleActivePerson(item)" 
        class="mr-2" 
        :color="item.active ? 'warning' : 'success'"
        :title="item.active ? 'Desativar' : 'Ativar'"
      >
        {{ item.active ? 'mdi-account-off' : 'mdi-account-check' }}
      </v-icon>
    </template>
  </v-data-table>
</template>

<script setup lang="ts">
import type PersonDto from '@/models/PersonDto'
import RemoteService from '@/services/RemoteService'
import CreatePersonDialog from './CreatePersonDialog.vue'
import EditPersonDialog from './EditPersonDialog.vue'
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const search = ref('')
const loading = ref(true)

const headers = [
  { title: 'ID', key: 'id', value: 'id', sortable: true, filterable: false },
  { title: 'Nome', key: 'name', value: 'name', sortable: true, filterable: true },
  { title: 'Email', key: 'email', value: 'email', sortable: true, filterable: true },
  { title: 'Escola', key: 'schoolCode', value: 'schoolCode', sortable: true, filterable: true },
  { title: 'Disciplina', key: 'subjectCode', value: 'subjectCode', sortable: true, filterable: true },
  { title: 'Tipo', key: 'type', value: 'type', sortable: true, filterable: true },
  { title: 'Estado', key: 'active', value: 'active', sortable: true, filterable: true },
  { title: 'Ações', key: 'actions', value: 'actions', sortable: false, filterable: false }
]

const people: PersonDto[] = reactive([])
const editDialogRef = ref()

getPeople()

async function getPeople() {
  loading.value = true
  people.splice(0, people.length)
  people.push(...(await RemoteService.getPeople()))
  loading.value = false
}

const editPerson = (person: PersonDto) => {
  editDialogRef.value.open(person)
}

const toggleActivePerson = async (person: PersonDto) => {
  if (!person.id) return 

  const actionText = person.active ? 'desativar' : 'ativar'

  if (confirm(`Tem a certeza que deseja ${actionText} o(a) ${person.name}?`)) {
    try {
      await RemoteService.togglePersonActive(person.id)
      await getPeople()
    } catch (error) {
      console.error(`Erro ao ${actionText} pessoa:`, error)
      alert(`Não foi possível ${actionText} esta pessoa.`)
    }
  }
}

const impersonate = async (person: PersonDto) => {
  if (!person.id) return 

  await auth.impersonate(person.id)
  await getPeople()
}

const customFilter = (value: any, query: string) => {
  if (value == null || !query) return false
  return String(value).toLowerCase().includes(query.trim().toLowerCase())
}
</script>