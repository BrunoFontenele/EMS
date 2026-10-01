<template>
  <v-container class="py-6">
    <template v-if="authStore.user?.role === 'STUDENT'">
      <v-row justify="center" class="mt-6">
        <v-col cols="12" md="8" lg="6">
          <v-card 
            hover 
            @click="router.push('/student/dashboard')" 
            class="pa-8 text-center elevation-3 rounded-lg border"
          >
            <v-avatar size="100" color="blue-lighten-5" class="mb-4">
              <v-icon size="56" color="primary">mdi-school</v-icon>
            </v-avatar>
            <h2 class="text-h4 font-weight-bold mb-2">Os Meus Exames</h2>
            <p class="text-body-1 text-grey-darken-1 mb-6">
              Consulta as tuas provas corrigidas, notas finais e submete pedidos de revisão de questões.
            </p>
            <v-btn color="primary" size="large" prepend-icon="mdi-arrow-right-circle" variant="flat">
              Aceder às Minhas Provas
            </v-btn>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <template v-else-if="authStore.user?.role === 'TEACHER'">
      <div class="mb-6">
        <div class="d-flex align-center gap-2 mb-1">
          <v-icon color="primary" size="28">mdi-account-tie</v-icon>
          <h2 class="text-h5 font-weight-bold">Área do Docente</h2>
        </div>
        <p class="text-body-2 text-medium-emphasis">
          Gestão de avaliações, correção de respostas e resposta a pedidos de revisão.
        </p>
      </div>

      <v-row justify="center">
        <v-col cols="12" md="6">
          <v-card
            hover
            @click="router.push('/corrections')"
            class="pa-6 rounded-xl elevation-2 h-100 border transition-swing"
            style="border-top: 5px solid #d32f2f !important;"
          >
            <div class="d-flex align-start justify-space-between mb-4">
              <v-avatar size="64" color="red-lighten-5" rounded="lg">
                <v-icon size="36" color="red-darken-2">mdi-check-decagram</v-icon>
              </v-avatar>
              <v-chip color="red" size="small" variant="tonal" class="font-weight-medium">
                Avaliação Ativa
              </v-chip>
            </div>

            <h3 class="text-h5 font-weight-bold mb-2">Zona de Correções</h3>
            <p class="text-body-2 text-medium-emphasis mb-6">
              Avalia as respostas distribuídas aos teus critérios, atribui cotações e regista notas por critério de correção.
            </p>

            <v-btn
              color="red-darken-1"
              variant="flat"
              prepend-icon="mdi-pencil-box-multiple-outline"
              block
            >
              Corrigir Provas
            </v-btn>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card
            hover
            @click="router.push('/reviews')"
            class="pa-6 rounded-xl elevation-2 h-100 border transition-swing"
            style="border-top: 5px solid #7b1fa2 !important;"
          >
            <div class="d-flex align-start justify-space-between mb-4">
              <v-avatar size="64" color="purple-lighten-5" rounded="lg">
                <v-icon size="36" color="purple-darken-2">mdi-message-alert-outline</v-icon>
              </v-avatar>
              <v-chip color="purple" size="small" variant="tonal" class="font-weight-medium">
                Recursos e Pedidos
              </v-chip>
            </div>

            <h3 class="text-h5 font-weight-bold mb-2">Pedidos de Revisão</h3>
            <p class="text-body-2 text-medium-emphasis mb-6">
              Analisa as justificações e contestações submetidas pelos alunos e revê a pontuação atribuída às questões.
            </p>

            <v-btn
              color="purple-darken-1"
              variant="flat"
              prepend-icon="mdi-clipboard-check-outline"
              block
            >
              Avaliar Pedidos de Revisão
            </v-btn>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <template v-else-if="authStore.user?.role === 'SCHOOL_STAFF'">
      <div class="mb-6">
        <div class="d-flex align-center gap-2 mb-1">
          <v-icon color="primary" size="28">mdi-clipboard-text-clock-outline</v-icon>
          <h2 class="text-h5 font-weight-bold">Gestão Operacional</h2>
        </div>
        <p class="text-body-2 text-medium-emphasis">
          Acesso e acompanhamento de exames, digitalizações e relatórios estatísticos.
        </p>
      </div>

      <v-row>
        <v-col cols="12" md="4">
          <v-card 
            hover 
            @click="router.push('/exams')" 
            class="pa-6 rounded-xl elevation-2 h-100 border"
            style="border-top: 4px solid #fb8c00 !important;"
          >
            <v-avatar size="56" color="orange-lighten-5" class="mb-4" rounded="lg">
              <v-icon size="32" color="orange-darken-2">mdi-file-document-edit-outline</v-icon>
            </v-avatar>
            <h3 class="text-h6 font-weight-bold mb-1">Exames</h3>
            <p class="text-body-2 text-medium-emphasis mb-4">
              Consultar provas, acompanhar estados de avaliação e gerir fluxos de revisão.
            </p>
            <v-btn variant="tonal" color="orange-darken-3" size="small" block>
              Abrir Exames
            </v-btn>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card 
            hover 
            @click="router.push('/questions')" 
            class="pa-6 rounded-xl elevation-2 h-100 border"
            style="border-top: 4px solid #3f51b5 !important;"
          >
            <v-avatar size="56" color="indigo-lighten-5" class="mb-4" rounded="lg">
              <v-icon size="32" color="indigo-darken-2">mdi-image-multiple-outline</v-icon>
            </v-avatar>
            <h3 class="text-h6 font-weight-bold mb-1">Questões e Recortes</h3>
            <p class="text-body-2 text-medium-emphasis mb-4">
              Gerir fragmentos e imagens de exames digitalizados e respetivas cotações.
            </p>
            <v-btn variant="tonal" color="indigo-darken-2" size="small" block>
              Ver Questões
            </v-btn>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card 
            hover 
            @click="router.push('/statistics')" 
            class="pa-6 rounded-xl elevation-2 h-100 border"
            style="border-top: 4px solid #0288d1 !important;"
          >
            <v-avatar size="56" color="light-blue-lighten-5" class="mb-4" rounded="lg">
              <v-icon size="32" color="light-blue-darken-2">mdi-chart-bar</v-icon>
            </v-avatar>
            <h3 class="text-h6 font-weight-bold mb-1">Estatísticas</h3>
            <p class="text-body-2 text-medium-emphasis mb-4">
              Acompanhar o progresso global de correções, métricas e relatórios da escola.
            </p>
            <v-btn variant="tonal" color="light-blue-darken-3" size="small" block>
              Consultar Relatórios
            </v-btn>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <template v-else>
      <h2 class="text-left mb-6">Menu Principal</h2>
      
      <v-row>
        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/people')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="primary" class="mb-4">mdi-account-group</v-icon>
            <h3>Gestão de Pessoal</h3>
            <p class="text-caption mt-2">Consultar, adicionar e editar utilizadores do sistema.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/schools')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="deep-purple" class="mb-4">mdi-domain</v-icon>
            <h3>Gestão de Escolas</h3>
            <p class="text-caption mt-2">Consultar, adicionar e editar escolas do sistema.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/subjects')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="teal" class="mb-4">mdi-book-multiple</v-icon>
            <h3>Gestão de Disciplinas</h3>
            <p class="text-caption mt-2">Consultar, adicionar e editar disciplinas do sistema.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/exams')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="orange" class="mb-4">mdi-file-document-edit</v-icon>
            <h3>Exames</h3>
            <p class="text-caption mt-2">Consultar provas, rever avaliações e gerir pedidos de revisão.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/questions')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="indigo" class="mb-4">mdi-image-multiple</v-icon>
            <h3>Questões</h3>
            <p class="text-caption mt-2">Gerir fragmentos de exames, visualizar imagens e cotações.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/corrections')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="red" class="mb-4">mdi-check-decagram</v-icon>
            <h3>Correções</h3>
            <p class="text-caption mt-2">Avaliar respostas e atribuir cotações às questões distribuídas.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/reviews')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="purple" class="mb-4">mdi-message-alert</v-icon>
            <h3>Revisões</h3>
            <p class="text-caption mt-2">Ler justificações e avaliar os pedidos de revisão dos alunos.</p>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="4">
          <v-card hover @click="router.push('/audit-logs')" class="pa-6 text-center elevation-2 h-100">
            <v-icon size="64" color="blue-grey-darken-2" class="mb-4">mdi-shield-search</v-icon>
            <h3>Auditoria</h3>
            <p class="text-caption mt-2">Consultar o registo de ações críticas e operações do sistema.</p>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
  
const router = useRouter()
const authStore = useAuthStore()
</script>

<style scoped>
.transition-swing {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.transition-swing:hover {
  transform: translateY(-4px);
}
</style>