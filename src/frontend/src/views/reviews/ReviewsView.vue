<template>
  <v-container>
    <div class="d-flex align-center mb-6">
      <h2>{{ isAdmin ? 'Todas as Revisões' : 'Revisões Atribuídas' }}</h2>
      <v-spacer></v-spacer>
      <v-btn color="primary" prepend-icon="mdi-refresh" :loading="loading" @click="loadReviews">
        Atualizar
      </v-btn>
    </div>

    <v-card elevation="2">
      <v-table hover>
        <thead>
          <tr>
            <th>ID Pedido</th>
            <th>ID Questão</th>
            <th>Professor Atribuído</th>
            <th>Estado</th>
            <th>Nota Antiga</th>
            <th>Nova Nota</th>
            <th class="text-center">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-4">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </td>
          </tr>
          <tr v-else-if="reviews.length === 0">
            <td colspan="7" class="text-center py-4 text-grey">
              Nenhuma revisão encontrada.
            </td>
          </tr>
          <tr v-for="review in reviews" :key="review.id ?? 0">
            <td>#{{ review.id }}</td>
            <td>{{ review.questionId }}</td>
            <td>
              <div v-if="review.assignedTeacherName || review.assignedTeacherEmail">
                <span class="font-weight-medium">{{ review.assignedTeacherName || review.assignedTeacherEmail }}</span>
                <div v-if="review.assignedTeacherName && review.assignedTeacherEmail" class="text-caption text-grey">
                  {{ review.assignedTeacherEmail }}
                </div>
              </div>
              <v-chip v-else size="x-small" color="grey">Não atribuído</v-chip>
            </td>
            <td>
              <v-chip :color="getStatusColor(review.reviewStatus)" size="small" class="font-weight-bold">
                {{ getStatusText(review.reviewStatus) }}
              </v-chip>
            </td>
            <td>{{ review.oldScore ?? 0 }} pts</td>
            <td>
              <span v-if="review.newScore !== null && review.newScore !== undefined" class="font-weight-bold text-success">
                {{ review.newScore }} pts
              </span>
              <span v-else class="text-grey">-</span>
            </td>
            <td class="text-center">
              <v-btn 
                color="primary" 
                variant="tonal" 
                size="small"
                @click="openReviewModal(review)"
              >
                {{ canGrade(review) ? 'Avaliar' : 'Ver Detalhes' }}
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <v-dialog v-model="dialog" max-width="800px">
      <v-card v-if="activeReview">
        <v-toolbar color="grey-darken-3" class="text-white">
          <v-toolbar-title>Revisão #{{ activeReview.id }}</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="dialog = false"></v-btn>
        </v-toolbar>

        <v-card-text class="pt-4">
          <v-alert type="info" variant="tonal" class="mb-4" density="compact">
            <strong>Professor Atribuído:</strong> 
            {{ activeReview.assignedTeacherName ? `${activeReview.assignedTeacherName} (${activeReview.assignedTeacherEmail})` : (activeReview.assignedTeacherEmail || 'Nenhum') }}
          </v-alert>

          <v-alert type="warning" variant="tonal" class="mb-4">
            <strong>Justificação do Aluno:</strong>
            <div class="mt-2 font-italic">"{{ activeReview.studentJustification }}"</div>
          </v-alert>

          <div class="text-center mb-4 pa-2 bg-grey-lighten-4 rounded">
            <v-progress-circular v-if="loadingImage" indeterminate color="primary"></v-progress-circular>
            <img 
              v-else-if="questionImageUrl" 
              :src="questionImageUrl" 
              style="max-width: 100%; max-height: 350px; object-fit: contain;" 
              class="rounded"
            />
            <div v-else class="text-error">Erro ao carregar imagem da pergunta.</div>
          </div>

          <v-divider class="mb-4"></v-divider>

          <v-row align="center" justify="center">
            <v-col cols="12" sm="4" class="text-center">
              <div class="text-subtitle-2 text-grey">Nota Anterior</div>
              <div class="text-h5 font-weight-bold">{{ activeReview.oldScore ?? 0 }} pts</div>
            </v-col>
            <v-col cols="12" sm="5">
              <v-text-field
                v-model.number="newScore"
                label="Nova Nota"
                type="number"
                min="0"
                variant="outlined"
                density="compact"
                hide-details
                :disabled="!canGrade(activeReview)"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-card-text>

        <v-card-actions class="pa-4 bg-grey-lighten-4 justify-end">
          <v-btn variant="text" @click="dialog = false">Fechar</v-btn>
          <v-btn 
            v-if="canGrade(activeReview)"
            color="success" 
            variant="flat" 
            @click="submitGrade"
            :loading="submitting"
            :disabled="newScore === null || newScore < 0"
          >
            Confirmar Avaliação
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteService from '@/services/RemoteService'
import { useAuthStore } from '@/stores/auth'
import { ReviewStatus, type ReviewDto } from '@/models/ReviewDto'

const authStore = useAuthStore()

const reviews = ref<ReviewDto[]>([])
const loading = ref(false)
const dialog = ref(false)
const activeReview = ref<ReviewDto | null>(null)
const newScore = ref<number | null>(null)

const questionImageUrl = ref<string | null>(null)
const loadingImage = ref(false)
const submitting = ref(false)
const snackbar = ref({ show: false, text: '', color: 'success' })

const isAdmin = computed(() => authStore.user?.role === 'ADMINISTRATOR')

onMounted(async () => {
  await loadReviews()
})

const loadReviews = async () => {
  loading.value = true
  try {
    reviews.value = await RemoteService.getReviews()
  } catch (error) {
    snackbar.value = { show: true, text: 'Erro ao carregar revisões.', color: 'error' }
  } finally {
    loading.value = false
  }
}

const canGrade = (review: ReviewDto | null) => {
  if (!review) return false
  if (review.reviewStatus === ReviewStatus.REVIEWED) return false
  return true
}

const getStatusColor = (status: ReviewStatus) => {
  switch (status) {
    case ReviewStatus.PENDING: return 'grey'
    case ReviewStatus.IN_REVIEW: return 'warning'
    case ReviewStatus.REVIEWED: return 'success'
    default: return 'info'
  }
}

const getStatusText = (status: ReviewStatus) => {
  switch (status) {
    case ReviewStatus.PENDING: return 'Pendente'
    case ReviewStatus.IN_REVIEW: return 'Em Revisão'
    case ReviewStatus.REVIEWED: return 'Concluída'
    default: return status
  }
}

const openReviewModal = async (review: ReviewDto) => {
  activeReview.value = review
  newScore.value = review.newScore !== null ? review.newScore : review.oldScore
  
  if (questionImageUrl.value) {
    window.URL.revokeObjectURL(questionImageUrl.value)
    questionImageUrl.value = null
  }

  dialog.value = true
  loadingImage.value = true

  try {
    const blob = await RemoteService.getQuestionImage(review.questionId)
    questionImageUrl.value = window.URL.createObjectURL(blob)
  } catch (error) {
    console.error('Erro ao carregar imagem', error)
  } finally {
    loadingImage.value = false
  }
}

const submitGrade = async () => {
  if (newScore.value === null || !activeReview.value?.id) return
  if (newScore.value < 0) {
    snackbar.value = { show: true, text: 'A nota não pode ser negativa.', color: 'error' }
    return
  }
  
  submitting.value = true
  try {
    await RemoteService.submitReviewGrade(activeReview.value.id, { newScore: newScore.value } as any)
    snackbar.value = { show: true, text: 'Revisão avaliada com sucesso!', color: 'success' }
    dialog.value = false
    await loadReviews()
  } catch (error) {
    snackbar.value = { show: true, text: 'Erro ao submeter nota.', color: 'error' }
  } finally {
    submitting.value = false
  }
}
</script>