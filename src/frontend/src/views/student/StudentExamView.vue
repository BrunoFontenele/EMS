<template>
  <v-container fluid class="h-100">
    <div class="d-flex align-center mb-4">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.push('/student/dashboard')"></v-btn>
      <h2 class="ml-2">Exame de {{ examDetails?.subjectCode }} - Detalhes</h2>
      <v-spacer></v-spacer>
      <v-chip color="primary" size="large" class="text-h6">Nota Final: {{ examDetails?.finalScore }} pts</v-chip>
    </div>

    <v-row class="h-100">
      <v-col cols="12" md="7">
        <v-card elevation="2" class="h-100 d-flex flex-column" style="min-height: 75vh; background-color: #525659;">
          <div class="d-flex justify-center align-center pa-2 bg-grey-darken-4 text-white">
            <v-btn icon="mdi-chevron-left" variant="text" color="white" @click="page--" :disabled="page <= 1"></v-btn>
            <span class="mx-4">Página {{ page }} de {{ pageCount }}</span>
            <v-btn icon="mdi-chevron-right" variant="text" color="white" @click="page++" :disabled="page >= pageCount"></v-btn>
          </div>

          <div class="flex-grow-1" style="overflow-y: auto; padding: 20px;">
            <v-progress-circular v-if="loadingPdf" indeterminate color="white" class="ma-4"></v-progress-circular>
            <VuePdfEmbed 
              v-else-if="pdfSource"
              :source="pdfSource"
              :page="page"
              @loaded="onPdfLoaded"
              style="width: 100%; box-shadow: 0 4px 8px rgba(0,0,0,0.3);"
            />
            <p v-else class="text-white text-center ma-4">Não foi possível carregar o PDF.</p>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" md="5">
        <v-card elevation="2" class="h-100 pa-4" style="max-height: 75vh; overflow-y: auto;">
          <h3 class="mb-4">Perguntas da Prova</h3>
          
          <v-alert 
            v-if="examDetails?.isReviewPeriodOpen" 
            type="info" 
            variant="tonal" 
            class="mb-4"
          >
            O período de revisão está aberto. Podes submeter pedidos de justificação para perguntas específicas.
          </v-alert>

          <v-expansion-panels>
            <v-expansion-panel v-for="q in examDetails?.fragments" :key="q.id">
              <v-expansion-panel-title>
                <div class="d-flex w-100 justify-space-between align-center">
                  <strong>Pergunta {{ q.number }}</strong>
                  <div class="d-flex align-center gap-2">
                    <v-chip 
                      v-if="isReviewed(q.review)" 
                      color="success" 
                      size="small" 
                      class="mr-2 font-weight-bold"
                    >
                      Revisado: {{ q.review.newScore }} / {{ q.maxScore }} pts
                    </v-chip>
                    <v-chip 
                      v-else 
                      :color="q.score === q.maxScore ? 'success' : 'warning'" 
                      size="small"
                    >
                      {{ q.score }} / {{ q.maxScore }} pts
                    </v-chip>
                  </div>
                </div>
              </v-expansion-panel-title>
              
              <v-expansion-panel-text>
                <div class="mb-4 text-center">
                  <v-progress-circular v-if="questionImages[q.id] === undefined" indeterminate color="primary" size="24"></v-progress-circular>
                  <div v-else-if="questionImages[q.id] === 'error'" class="text-caption text-error">
                    Erro ao carregar a imagem.
                  </div>
                  <v-img 
                    v-else 
                    :src="questionImages[q.id]" 
                    max-height="150" 
                    class="bg-grey-lighten-3 rounded"
                    contain
                  ></v-img>
                </div>

                <div v-if="isReviewed(q.review)">
                  <v-alert type="success" variant="tonal" class="mt-2 text-caption">
                    <div class="d-flex align-center font-weight-bold mb-1">
                      <v-icon color="success" class="mr-1" size="small">mdi-check-decagram</v-icon>
                      Avaliado pelo Professor!
                    </div>
                    <div class="text-body-2 my-2">
                      Nota Anterior: 
                      <span class="text-decoration-line-through text-grey-darken-1 font-weight-bold">
                        {{ q.review.oldScore ?? q.score }} pts
                      </span> 
                      &nbsp;➔&nbsp; 
                      Nova Nota: 
                      <strong class="text-success text-h6">{{ q.review.newScore }} pts</strong>
                    </div>
                    <div class="text-grey-darken-2 mt-1">
                      <em>A tua justificação: "{{ q.review.studentJustification }}"</em>
                    </div>
                  </v-alert>
                </div>

                <div v-else-if="q.review">
                  <v-alert type="warning" variant="tonal" class="mt-2 text-caption">
                    <div class="font-weight-bold">Pedido em Análise pelo Professor</div>
                    <div class="mt-1 font-italic text-grey-darken-2">"{{ q.review.studentJustification }}"</div>
                  </v-alert>
                </div>

                <div v-else-if="examDetails?.isReviewPeriodOpen" class="mt-2">
                  <v-textarea
                    v-model="justifications[q.id]"
                    label="Justificação para Revisão"
                    variant="outlined"
                    rows="3"
                    hide-details
                    class="mb-2"
                  ></v-textarea>
                  <v-btn 
                    color="primary" 
                    size="small" 
                    block 
                    @click="submitReview(q.id)"
                    :disabled="!justifications[q.id]"
                  >
                    Submeter Pedido
                  </v-btn>
                </div>

                <div v-else class="text-caption text-grey mt-2">
                  O período de revisão já terminou.
                </div>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-card>
      </v-col>
    </v-row>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000" location="top">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import VuePdfEmbed from 'vue-pdf-embed'
import RemoteService from '@/services/RemoteService'
import type SubmitReviewDto from '@/models/SubmitReviewDto'

const route = useRoute()
const examId = Number(route.params.id)

const examDetails = ref<any>(null)
const justifications = ref<Record<number, string>>({})
const snackbar = ref({ show: false, text: '', color: 'success' })

const pdfSource = ref<any>(null)
const loadingPdf = ref(true)
const page = ref(1)
const pageCount = ref(1)
const myReviews = ref<any[]>([])

const questionImages = ref<Record<number, string>>({}) 

onMounted(async () => {
  await loadExamData()
  await loadPdf()
})

const isReviewed = (review: any) => {
  return !!review && (review.newScore !== null && review.newScore !== undefined || review.reviewStatus === 'REVIEWED')
}

const loadExamData = async () => {
  try {
    examDetails.value = await RemoteService.getStudentExamDetails(examId)
    myReviews.value = await RemoteService.getReviews()
    
    if (examDetails.value?.fragments) {
      examDetails.value.fragments.sort((a: any, b: any) => a.number - b.number)
      
      examDetails.value.fragments.forEach((frag: any) => {
        const reviewFound = myReviews.value.find(r => r.questionId === frag.id)
        if (reviewFound) {
          frag.review = reviewFound
        }
      })
      
      for (const frag of examDetails.value.fragments) {
        loadQuestionImage(frag.id)
      }
    }
  } catch (error) {
    snackbar.value = { show: true, text: 'Erro ao carregar detalhes do exame.', color: 'error' }
  }
}

const loadQuestionImage = async (questionId: number) => {
  try {
    const blob = await RemoteService.getQuestionImage(questionId)
    questionImages.value[questionId] = window.URL.createObjectURL(blob)
  } catch (error) {
    console.error(`Erro ao carregar imagem para a pergunta ${questionId}:`, error)
    questionImages.value[questionId] = 'error'
  }
}

const loadPdf = async () => {
  loadingPdf.value = true
  try {
    const blob = await RemoteService.getExamPdf(examId)
    pdfSource.value = window.URL.createObjectURL(blob)
  } catch (error) {
    console.error("Erro a carregar PDF:", error)
  } finally {
    loadingPdf.value = false
  }
}

const onPdfLoaded = (doc: any) => {
  pageCount.value = doc.numPages || 1
}

const submitReview = async (questionId: number) => {
  const text = justifications.value[questionId]
  if (!text) return

  const dto: SubmitReviewDto = {
    questionId: questionId,
    justification: text
  }

  try {
    await RemoteService.submitReviewRequest(examId, dto)
    snackbar.value = { show: true, text: 'Pedido de revisão submetido com sucesso!', color: 'success' }
    justifications.value[questionId] = ''
    await loadExamData() 
  } catch (error) {
    snackbar.value = { show: true, text: 'Erro ao submeter pedido.', color: 'error' }
  }
}
</script>