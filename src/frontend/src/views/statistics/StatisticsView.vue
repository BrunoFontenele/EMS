<template>
  <v-container fluid class="pa-6">
    <div class="d-flex align-center mb-6">
      <div>
        <h2 class="text-h5 font-weight-bold">Estatísticas Académicas da Escola</h2>
        <div class="text-caption text-grey-darken-1">
          Distribuição gráfica e métricas agregadas por disciplina
        </div>
      </div>
    </div>

    <!-- Filtro de Pesquisa -->
    <v-card elevation="1" class="pa-4 mb-6 rounded-lg">
      <v-row align="center">
        <v-col cols="12" sm="8" md="5">
          <v-text-field
            v-model="subjectInput"
            label="Código da Disciplina (ex: MAT-A, FIS)"
            variant="outlined"
            density="comfortable"
            hide-details
            prepend-inner-icon="mdi-book-search-outline"
            @keyup.enter="searchStatistics"
          ></v-text-field>
        </v-col>
        <v-col cols="12" sm="4" md="3">
          <v-btn 
            color="primary" 
            size="large" 
            block 
            prepend-icon="mdi-chart-box-outline" 
            :loading="loading" 
            @click="searchStatistics"
          >
            Analisar Resultados
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <!-- Estado de Loading -->
    <v-row v-if="loading">
      <v-col class="text-center py-12" cols="12">
        <v-progress-circular indeterminate color="primary" size="56"></v-progress-circular>
        <div class="text-caption text-grey mt-3">A compilar indicadores e gráficos...</div>
      </v-col>
    </v-row>

    <!-- Sem Dados -->
    <div v-else-if="searched && !stats">
      <v-alert type="warning" variant="tonal" icon="mdi-alert-circle-outline">
        Não foram encontrados exames com notas atribuídas para a disciplina indicada.
      </v-alert>
    </div>

    <!-- Dashboard Completo -->
    <div v-else-if="stats">
      <!-- Cabeçalho -->
      <v-card elevation="1" class="mb-6 pa-4 bg-primary text-white rounded-lg">
        <div class="d-flex flex-wrap justify-space-between align-center">
          <div>
            <div class="text-overline">{{ stats.schoolName }} ({{ stats.schoolCode }})</div>
            <div class="text-h5 font-weight-bold">{{ stats.subjectName }}</div>
            <div class="text-subtitle-2">Sigla: {{ stats.subjectCode }}</div>
          </div>
          <div class="text-right">
            <v-chip color="white" text-color="primary" class="font-weight-bold" size="large">
              {{ stats.totalStudents }} Alunos Avaliados
            </v-chip>
          </div>
        </div>
      </v-card>

      <!-- KPI Cards -->
      <v-row class="mb-6">
        <v-col cols="12" sm="6" md="3">
          <v-card elevation="2" class="pa-4 h-100 rounded-lg stat-card">
            <div class="d-flex justify-space-between align-center">
              <div>
                <div class="text-caption text-grey-darken-1 font-weight-medium">Média Global</div>
                <div class="text-h4 font-weight-bold text-primary mt-1">
                  {{ stats.averageScore ?? 0 }} <span class="text-body-2 text-grey">pts</span>
                </div>
              </div>
              <v-avatar color="primary-lighten-5" size="48">
                <v-icon color="primary" size="28">mdi-calculator-variant-outline</v-icon>
              </v-avatar>
            </div>
            <v-progress-linear 
              :model-value="stats.averageScore" 
              color="primary" 
              height="6" 
              rounded 
              class="mt-4"
            ></v-progress-linear>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card elevation="2" class="pa-4 h-100 rounded-lg stat-card">
            <div class="d-flex justify-space-between align-center">
              <div>
                <div class="text-caption text-grey-darken-1 font-weight-medium">Taxa de Aprovação</div>
                <div class="text-h4 font-weight-bold text-success mt-1">
                  {{ stats.approvalRate }}%
                </div>
              </div>
              <v-avatar color="success-lighten-5" size="48">
                <v-icon color="success" size="28">mdi-account-check-outline</v-icon>
              </v-avatar>
            </div>
            <div class="text-caption text-grey mt-3">
              <strong>{{ stats.approvedCount }}</strong> aprovados / <strong>{{ stats.failedCount }}</strong> reprovados
            </div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card elevation="2" class="pa-4 h-100 rounded-lg stat-card">
            <div class="d-flex justify-space-between align-center">
              <div>
                <div class="text-caption text-grey-darken-1 font-weight-medium">Nota Mais Alta</div>
                <div class="text-h4 font-weight-bold text-success mt-1">
                  {{ stats.highestScore ?? '-' }} <span class="text-body-2 text-grey">pts</span>
                </div>
              </div>
              <v-avatar color="success-lighten-5" size="48">
                <v-icon color="success" size="28">mdi-trophy-outline</v-icon>
              </v-avatar>
            </div>
            <div class="text-caption text-grey mt-3">Melhor pontuação registada</div>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card elevation="2" class="pa-4 h-100 rounded-lg stat-card">
            <div class="d-flex justify-space-between align-center">
              <div>
                <div class="text-caption text-grey-darken-1 font-weight-medium">Nota Mais Baixa</div>
                <div class="text-h4 font-weight-bold text-error mt-1">
                  {{ stats.lowestScore ?? '-' }} <span class="text-body-2 text-grey">pts</span>
                </div>
              </div>
              <v-avatar color="error-lighten-5" size="48">
                <v-icon color="error" size="28">mdi-arrow-down-bold-box-outline</v-icon>
              </v-avatar>
            </div>
            <div class="text-caption text-grey mt-3">Menor pontuação registada</div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Secção de Gráficos (Chart.js) -->
      <v-row class="mb-6">
        <!-- Gráfico 1: Histograma de Distribuição de Notas -->
        <v-col cols="12" md="8">
          <v-card elevation="2" class="pa-4 h-100 rounded-lg">
            <v-card-title class="pa-0 mb-3 text-subtitle-1 font-weight-bold">
              <v-icon class="mr-2" color="primary">mdi-chart-bar</v-icon>
              Distribuição por Intervalos de Classificação
            </v-card-title>
            <div style="height: 280px; position: relative;">
              <Bar :data="barChartData" :options="barChartOptions" />
            </div>
          </v-card>
        </v-col>

        <!-- Gráfico 2: Donut Chart de Aprovações -->
        <v-col cols="12" md="4">
          <v-card elevation="2" class="pa-4 h-100 rounded-lg">
            <v-card-title class="pa-0 mb-3 text-subtitle-1 font-weight-bold">
              <v-icon class="mr-2" color="success">mdi-chart-donut</v-icon>
              Rácio de Aproveitamento
            </v-card-title>
            <div style="height: 280px; position: relative;" class="d-flex align-center justify-center">
              <Doughnut :data="doughnutChartData" :options="doughnutChartOptions" />
            </div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Tabela Nominal -->
      <v-card elevation="2" class="rounded-lg">
        <v-card-item class="pa-4 bg-grey-lighten-4">
          <div class="d-flex justify-space-between align-center">
            <div>
              <v-card-title class="font-weight-bold pa-0">Pauta de Exames e Classificações</v-card-title>
              <v-card-subtitle class="pa-0">Resultados dos alunos com provas fechadas ou publicadas</v-card-subtitle>
            </div>
            <v-text-field
              v-model="tableSearch"
              prepend-inner-icon="mdi-magnify"
              label="Pesquisar Aluno"
              density="compact"
              variant="outlined"
              hide-details
              style="max-width: 260px;"
            ></v-text-field>
          </div>
        </v-card-item>

        <v-divider></v-divider>

        <v-data-table
          :headers="headers"
          :items="stats.studentResults"
          :search="tableSearch"
          item-key="examId"
          hover
          no-data-text="Nenhum registo disponível."
        >
          <template #item.finalScore="{ item }">
            <v-chip 
              :color="(item.finalScore ?? 0) >= 50 ? 'success' : 'error'" 
              size="small" 
              class="font-weight-bold"
              variant="flat"
            >
              {{ item.finalScore }} pts
            </v-chip>
          </template>
        </v-data-table>
      </v-card>
    </div>

    <!-- Notificações -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import RemoteService from '@/services/RemoteService'
import type SchoolSubjectStatisticsDto from '@/models/StatisticsDto'

// Registar componentes e módulos do Chart.js
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement
} from 'chart.js'
import { Bar, Doughnut } from 'vue-chartjs'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement)

const subjectInput = ref('')
const tableSearch = ref('')
const loading = ref(false)
const searched = ref(false)
const stats = ref<SchoolSubjectStatisticsDto | null>(null)
const snackbar = ref({ show: false, text: '', color: 'success' })

const headers = [
  { title: 'ID Exame', key: 'examId' },
  { title: 'Nome do Aluno', key: 'studentName' },
  { title: 'Email Institucional', key: 'studentEmail' },
  { title: 'Classificação Final', key: 'finalScore', align: 'center' as const }
]

const searchStatistics = async () => {
  const code = subjectInput.value.trim().toUpperCase()
  if (!code) {
    snackbar.value = { show: true, text: 'Introduza a sigla da disciplina.', color: 'warning' }
    return
  }

  loading.value = true
  searched.value = true

  try {
    stats.value = await RemoteService.getSchoolExamStatistics(code)
  } catch (error) {
    console.error('Erro ao obter estatísticas:', error)
    snackbar.value = { show: true, text: 'Erro ao carregar dados da disciplina.', color: 'error' }
    stats.value = null
  } finally {
    loading.value = false
  }
}

// Configuração do Gráfico de Barras (Histograma por escalões de 20 em 20 pontos)
const barChartData = computed(() => {
  if (!stats.value?.studentResults) {
    return { labels: [], datasets: [] }
  }

  const buckets = [0, 0, 0, 0, 0] // 0-19, 20-39, 40-59, 60-79, 80-100
  stats.value.studentResults.forEach(student => {
    const score = student.finalScore ?? 0
    if (score < 20) buckets[0]++
    else if (score < 40) buckets[1]++
    else if (score < 60) buckets[2]++
    else if (score < 80) buckets[3]++
    else buckets[4]++
  })

  return {
    labels: ['0 - 19 pts', '20 - 39 pts', '40 - 59 pts', '60 - 79 pts', '80 - 100 pts'],
    datasets: [
      {
        label: 'Nº de Alunos',
        backgroundColor: ['#E53935', '#FB8C00', '#FDD835', '#43A047', '#1E88E5'],
        borderRadius: 6,
        data: buckets
      }
    ]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: { stepSize: 1 }
    }
  }
}

// Configuração do Gráfico de Donut (Aprovados vs Reprovados)
const doughnutChartData = computed(() => {
  if (!stats.value) {
    return { labels: [], datasets: [] }
  }

  return {
    labels: ['Aprovados (>= 50 pts)', 'Reprovados (< 50 pts)'],
    datasets: [
      {
        backgroundColor: ['#4CAF50', '#F44336'],
        hoverOffset: 4,
        data: [stats.value.approvedCount, stats.value.failedCount]
      }
    ]
  }
})

const doughnutChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom' as const
    }
  }
}
</script>

<style scoped>
.stat-card {
  border: 1px solid rgba(0, 0, 0, 0.06);
}
.bg-primary-lighten-5 {
  background-color: rgba(var(--v-theme-primary), 0.1);
}
.bg-success-lighten-5 {
  background-color: rgba(var(--v-theme-success), 0.1);
}
.bg-error-lighten-5 {
  background-color: rgba(var(--v-theme-error), 0.1);
}
</style>