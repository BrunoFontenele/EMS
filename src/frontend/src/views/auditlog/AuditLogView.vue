<template>
  <v-container fluid class="py-6 px-4 px-md-8">
    <!-- Cabeçalho Principal -->
    <div class="d-flex flex-column flex-sm-row align-start align-sm-center justify-space-between gap-4 mb-6">
      <div>
        <div class="d-flex align-center gap-2 mb-1">
          <v-avatar color="blue-grey-lighten-5" size="40" rounded="lg">
            <v-icon color="blue-grey-darken-3" size="24">mdi-shield-check</v-icon>
          </v-avatar>
          <h1 class="text-h5 font-weight-bold text-blue-grey-darken-4">Registo de Auditoria</h1>
        </div>
        <p class="text-body-2 text-medium-emphasis">
          Histórico inviolável de operações, eventos críticos e acessos ao sistema.
        </p>
      </div>

      <v-btn
        color="blue-grey-darken-3"
        variant="flat"
        prepend-icon="mdi-refresh"
        @click="fetchLogs"
        :loading="loading"
        class="elevation-1"
      >
        Atualizar Dados
      </v-btn>
    </div>

    <!-- Cards de Métricas / KPIs -->
    <v-row class="mb-6">
      <v-col cols="12" sm="4">
        <v-card class="pa-4 rounded-xl border elevation-0 bg-surface">
          <div class="d-flex align-center justify-space-between">
            <div>
              <div class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Total de Registos</div>
              <div class="text-h4 font-weight-bold mt-1">{{ logs.length }}</div>
            </div>
            <v-avatar color="blue-lighten-5" size="48" rounded="lg">
              <v-icon color="blue-darken-2" size="28">mdi-format-list-numbered</v-icon>
            </v-avatar>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" sm="4">
        <v-card class="pa-4 rounded-xl border elevation-0 bg-surface">
          <div class="d-flex align-center justify-space-between">
            <div>
              <div class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Eventos Hoje</div>
              <div class="text-h4 font-weight-bold mt-1 text-teal-darken-2">{{ todayEventsCount }}</div>
            </div>
            <v-avatar color="teal-lighten-5" size="48" rounded="lg">
              <v-icon color="teal-darken-2" size="28">mdi-calendar-today</v-icon>
            </v-avatar>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" sm="4">
        <v-card class="pa-4 rounded-xl border elevation-0 bg-surface">
          <div class="d-flex align-center justify-space-between">
            <div>
              <div class="text-caption font-weight-bold text-uppercase text-medium-emphasis">Utilizadores Ativos</div>
              <div class="text-h4 font-weight-bold mt-1 text-indigo-darken-2">{{ uniqueUsersCount }}</div>
            </div>
            <v-avatar color="indigo-lighten-5" size="48" rounded="lg">
              <v-icon color="indigo-darken-2" size="28">mdi-account-group-outline</v-icon>
            </v-avatar>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <!-- Tabela Principal com Pesquisa Integrada -->
    <v-card class="rounded-xl border elevation-0 overflow-hidden">
      <v-card-title class="pa-4 bg-grey-lighten-5 border-b d-flex flex-column flex-md-row align-center justify-space-between gap-4">
        <div class="d-flex align-center gap-2">
          <v-icon size="20" color="blue-grey-darken-2">mdi-history</v-icon>
          <span class="text-subtitle-1 font-weight-bold">Fluxo de Eventos</span>
        </div>

        <div style="min-width: 320px; max-width: 450px;" class="w-100">
          <v-text-field
            v-model="search"
            placeholder="Pesquisar por email, ação ou data..."
            prepend-inner-icon="mdi-magnify"
            variant="solo"
            density="compact"
            flat
            bg-color="white"
            hide-details
            clearable
            class="elevation-1 rounded-lg"
          ></v-text-field>
        </div>
      </v-card-title>

      <v-data-table
        :headers="headers"
        :items="logs"
        :search="search"
        :loading="loading"
        hover
        :items-per-page="10"
        item-key="id"
        class="text-left"
        no-data-text="Nenhum registo de auditoria encontrado."
      >
        <!-- Formatação da Data e Hora -->
        <template #item.timestamp="{ item }">
          <div class="d-flex align-center gap-2 py-2">
            <v-icon size="16" color="medium-emphasis">mdi-clock-outline</v-icon>
            <div>
              <div class="text-body-2 font-weight-medium text-blue-grey-darken-4">
                {{ formatDateTime(item.timestamp).date }}
              </div>
              <div class="text-caption text-medium-emphasis">
                {{ formatDateTime(item.timestamp).time }}
              </div>
            </div>
          </div>
        </template>

        <!-- Formatação do Utilizador -->
        <template #item.userEmail="{ item }">
          <div class="d-flex align-center gap-2">
            <v-avatar size="28" :color="item.userEmail === 'SYSTEM' ? 'grey-lighten-3' : 'blue-lighten-5'">
              <v-icon size="16" :color="item.userEmail === 'SYSTEM' ? 'grey-darken-2' : 'primary'">
                {{ item.userEmail === 'SYSTEM' ? 'mdi-cog' : 'mdi-account' }}
              </v-icon>
            </v-avatar>
            <span class="text-body-2 font-weight-medium">{{ item.userEmail }}</span>
          </div>
        </template>

        <!-- Formatação da Ação com Tags Semânticas -->
        <template #item.action="{ item }">
          <div class="d-flex align-center gap-2 py-2 flex-wrap">
            <v-chip
              size="small"
              variant="tonal"
              :color="getActionBadge(item.action).color"
              :prepend-icon="getActionBadge(item.action).icon"
              class="font-weight-medium"
            >
              {{ getActionBadge(item.action).label }}
            </v-chip>
            <span class="text-body-2 text-high-emphasis">{{ item.action }}</span>
          </div>
        </template>
      </v-data-table>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteServices from '@/services/RemoteService'

interface AuditLogDto {
  id?: number
  userEmail: string
  action: string
  timestamp: string
}

const search = ref('')
const loading = ref(false)
const logs = ref<AuditLogDto[]>([])

const headers = [
  { title: 'Data e Hora', key: 'timestamp', value: 'timestamp', sortable: true, width: '200px' },
  { title: 'Autor da Ação', key: 'userEmail', value: 'userEmail', sortable: true, width: '250px' },
  { title: 'Operação Registada', key: 'action', value: 'action', sortable: true }
]

// KPIs computados em tempo real
const todayEventsCount = computed(() => {
  const todayStr = new Date().toISOString().slice(0, 10)
  return logs.value.filter((l) => l.timestamp && l.timestamp.startsWith(todayStr)).length
})

const uniqueUsersCount = computed(() => {
  const users = new Set(logs.value.map((l) => l.userEmail))
  return users.size
})

// Função para formatar data de forma limpa
function formatDateTime(isoString: string) {
  if (!isoString) return { date: '-', time: '' }
  const d = new Date(isoString)
  return {
    date: d.toLocaleDateString('pt-PT', { day: '2-digit', month: 'short', year: 'numeric' }),
    time: d.toLocaleTimeString('pt-PT', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  }
}

// Detetor inteligente de categorias de ação
function getActionBadge(actionText: string) {
  const lower = (actionText || '').toLowerCase()
  if (lower.includes('criou') || lower.includes('submeteu') || lower.includes('adicionou')) {
    return { color: 'teal', icon: 'mdi-plus-circle-outline', label: 'Criação' }
  }
  if (lower.includes('atualizou') || lower.includes('alterou') || lower.includes('avaliou')) {
    return { color: 'blue', icon: 'mdi-pencil-outline', label: 'Edição' }
  }
  if (lower.includes('ativou') || lower.includes('desativou')) {
    return { color: 'amber-darken-3', icon: 'mdi-toggle-switch-outline', label: 'Estado' }
  }
  if (lower.includes('eliminou') || lower.includes('removeu')) {
    return { color: 'red', icon: 'mdi-delete-outline', label: 'Eliminação' }
  }
  return { color: 'blue-grey', icon: 'mdi-information-outline', label: 'Sistema' }
}

async function fetchLogs() {
  loading.value = true
  try {
    const data = await RemoteServices.getAuditLogs()
    logs.value = data || []
  } catch (error) {
    console.error('Erro ao carregar auditoria:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}
.gap-4 {
  gap: 16px;
}
</style>