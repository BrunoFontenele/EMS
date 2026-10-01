<template>
  <v-container>
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" @click="$router.push('/exams')" class="mr-4"></v-btn>
      <h2 v-if="examDetails">
        Separar Exame: {{ examDetails.studentEmail }} — {{ examDetails.subjectCode }}
      </h2>
      <h2 v-else>A carregar exame...</h2>
    </div>
    
    <v-row>
      <v-col cols="12" md="7">
        <v-card class="pa-4" elevation="2">
          <v-card-title>Documento Original</v-card-title>
          
          <div class="d-flex align-center justify-center mb-4">
            <v-btn icon="mdi-chevron-left" @click="page--" :disabled="page <= 1" variant="tonal"></v-btn>
            <span class="mx-6 font-weight-bold">Página {{ page }} de {{ pageCount }}</span>
            <v-btn icon="mdi-chevron-right" @click="page++" :disabled="page >= pageCount" variant="tonal"></v-btn>
          </div>

          <div ref="pdfContainer" style="border: 1px solid #ccc; max-height: 650px; overflow-y: auto; background: #555;" class="d-flex justify-center">
            <VuePdfEmbed 
              v-if="pdfSource"
              :source="pdfSource" 
              :page="page" 
              :scale="2.5" 
              @loaded="onPdfLoaded"
            />
            <v-progress-circular v-else indeterminate color="primary"></v-progress-circular>
          </div>
        </v-card>
      </v-col>

      <v-col cols="12" md="5">
        <v-card class="pa-4" elevation="2">
          <v-card-title>Registar Pergunta</v-card-title>
          <v-card-text>
            
            <v-alert v-if="isPageExtracted" type="success" variant="tonal" class="mb-4" prepend-icon="mdi-check-circle">
              A <strong>Página {{ page }}</strong> já foi guardada como Questão.
            </v-alert>

            <v-alert v-else type="info" variant="tonal" class="mb-4">
              Ao extrair, a <strong>Página {{ page }}</strong> inteira será guardada como a Questão {{ page }}.
            </v-alert>
            
            <v-text-field 
              v-model="question.maxScore" 
              label="Cotação Máxima*" 
              type="number"
              min="0"
              :disabled="isPageExtracted"
            ></v-text-field>
            
            <v-btn 
              color="primary" 
              block 
              size="large"
              prepend-icon="mdi-camera-plus"
              @click="extractAndSave"
              :disabled="isPageExtracted || !question.maxScore || question.maxScore <= 0"
            >
              Extrair Página e Guardar
            </v-btn>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VuePdfEmbed from 'vue-pdf-embed'
import RemoteService from '@/services/RemoteService'

const route = useRoute()
const router = useRouter()
const examId = route.params.id

const pdfSource = ref<any>(null)
const page = ref(1)
const pageCount = ref(1)
const pdfContainer = ref<HTMLElement | null>(null)

const examDetails = ref<any>(null)
const question = ref({ maxScore: 0 })

const extractedPages = ref<number[]>([])

const isPageExtracted = computed(() => extractedPages.value.includes(page.value))

onMounted(async () => {
  try {
    examDetails.value = await RemoteService.getExam(Number(examId))
    const blob = await RemoteService.getExamPdf(Number(examId))
    pdfSource.value = window.URL.createObjectURL(blob)

    const allQuestions = await RemoteService.getQuestions()
    
    extractedPages.value = allQuestions
      .filter((q: any) => q.examId === Number(examId))
      .map((q: any) => q.questionNumber) 

  } catch (error) {
    console.error("Erro ao carregar os dados:", error)
  }
})

const onPdfLoaded = (document: any) => {
  pageCount.value = document.numPages
}

const extractAndSave = async () => {
  if (!pdfContainer.value || !examDetails.value || isPageExtracted.value) return

  const canvas = pdfContainer.value.querySelector('canvas')
  if (!canvas) {
    alert("Ainda a renderizar a página, aguarde um segundo.")
    return
  }

  canvas.toBlob(async (blob) => {
    if (!blob) {
      alert("Erro ao processar a imagem da página.")
      return
    }

    const questionDto = {
      questionNumber: page.value,
      maxScore: Number(question.value.maxScore),
      examId: Number(examId),
      subjectCode: examDetails.value.subjectCode
    }

    const formData = new FormData()
    formData.append('question', new Blob([JSON.stringify(questionDto)], { type: 'application/json' }))
    formData.append('file', blob, `pergunta_${page.value}.jpg`)

    try {
      await RemoteService.createQuestion(formData)
      
      extractedPages.value.push(page.value)

      question.value.maxScore = 0
      if (page.value < pageCount.value) page.value++
      
    } catch (error) {
      console.error("Erro ao guardar a questão:", error)
      alert("Não foi possível guardar a questão.")
    }
  }, 'image/jpeg', 0.8)
}
</script>