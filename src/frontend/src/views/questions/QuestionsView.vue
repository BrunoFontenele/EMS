<template>
  <div>
    <v-container>
      <div class="d-flex align-center mb-6">
        <h2>Gestão de Questões (Fragmentos)</h2>
        <v-spacer></v-spacer>
        <v-btn 
          color="success" 
          prepend-icon="mdi-account-arrow-right" 
          @click="openDistributeDialog" 
          class="mr-2"
        >
          Distribuir Questões
        </v-btn>
        <v-btn color="primary" prepend-icon="mdi-refresh" @click="loadQuestions">Atualizar</v-btn>
      </div>

      <v-card elevation="2">
        <v-data-table
          :headers="headers"
          :items="questions"
          :loading="loading"
          hover
        >
          <template #item.id="{ item }">
            <div class="text-start">{{ item.id }}</div>
          </template>

          <template #item.examId="{ item }">
            <div class="text-center">{{ item.examId }}</div>
          </template>

          <template #item.subjectCode="{ item }">
            <div class="text-center">{{ item.subjectCode }}</div>
          </template>

          <template #item.questionNumber="{ item }">
            <div class="text-center">{{ item.questionNumber  }}</div>
          </template>

          <template #item.maxScore="{ item }">
            <div class="text-center"><strong>{{ item.maxScore }} pts</strong></div>
          </template>

          <template #item.actions="{ item }">
            <div class="d-flex justify-end">
              <v-tooltip text="Ver Imagem da Questão" location="top">
                <template v-slot:activator="{ props }">
                  <v-btn 
                    v-bind="props" 
                    icon="mdi-image" 
                    variant="text" 
                    size="small" 
                    color="info" 
                    @click="openImage(item)"
                  ></v-btn>
                </template>
              </v-tooltip>

              <v-tooltip text="Eliminar Questão" location="top">
                <template v-slot:activator="{ props }">
                  <v-btn 
                    v-bind="props" 
                    icon="mdi-delete" 
                    variant="text" 
                    size="small" 
                    color="error" 
                    @click="deleteItem(item)"
                  ></v-btn>
                </template>
              </v-tooltip>
            </div>
          </template>
        </v-data-table>
      </v-card>

      <v-dialog v-model="imageDialog" max-width="1000px">
        <v-card>
          <v-toolbar color="primary" title="Visualização da Questão"></v-toolbar>
          
          <v-card-text class="pa-4 text-center" style="background-color: #555; max-height: 75vh; overflow-y: auto;">
            <v-progress-circular v-if="loadingImage" indeterminate color="white"></v-progress-circular>
            
            <img 
              v-else-if="currentImageUrl" 
              :src="currentImageUrl" 
              alt="Imagem da Questão" 
              style="width: 100%; border: 1px solid #ccc; box-shadow: 0 4px 8px rgba(0,0,0,0.3); display: block; margin: 0 auto;" 
            />
            <p v-else class="text-white mt-4">Não foi possível carregar a imagem.</p>
          </v-card-text>
          
          <v-card-actions>
            <v-btn 
              color="info" 
              prepend-icon="mdi-open-in-new" 
              @click="openInNewTab"
              v-if="currentImageUrl"
            >
              Abrir em Tamanho Real
            </v-btn>
            
            <v-spacer></v-spacer>
            <v-btn color="error" variant="text" @click="closeImage">Fechar</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <v-dialog v-model="distributeDialog" max-width="450px">
        <v-card>
          <v-toolbar color="success" title="Distribuir Questões"></v-toolbar>
          <v-card-text class="pa-4">
            <p class="mb-4">Introduz o código da disciplina para efetuar a distribuição automática:</p>
            <v-text-field
              v-model="targetSubjectCode"
              label="Código da Disciplina (ex: MAT-A)"
              variant="outlined"
              hide-details
            ></v-text-field>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="grey-darken-1" variant="text" @click="distributeDialog = false">Cancelar</v-btn>
            <v-btn color="success" variant="flat" @click="confirmDistribute">Distribuir</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="4000" location="top">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemoteService from '@/services/RemoteService'
import type QuestionDto from '@/models/QuestionDto'

const questions = ref<QuestionDto[]>([])
const loading = ref(true)

const imageDialog = ref(false)
const loadingImage = ref(false)
const currentImageUrl = ref<string | null>(null)

const distributeDialog = ref(false)
const targetSubjectCode = ref('')

const snackbar = ref({
  show: false,
  text: '',
  color: 'success'
})

const headers = [
  { title: 'ID', key: 'id', align: 'start' as const },
  { title: 'ID do Exame', key: 'examId', align: 'center' as const },
  { title: 'Disciplina', key: 'subjectCode', align: 'center' as const },
  { title: 'Nº da Pergunta', key: 'questionNumber', align: 'center' as const },
  { title: 'Cotação', key: 'maxScore', align: 'center' as const },
  { title: 'Ações', key: 'actions', sortable: false, align: 'end' as const }
]

onMounted(async () => {
  await loadQuestions()
})

const loadQuestions = async () => {
  loading.value = true
  try {
    questions.value = await RemoteService.getQuestions()
  } catch (error) {
    console.error("Erro ao carregar questões:", error)
    alert("Erro ao carregar a lista de questões.")
  } finally {
    loading.value = false
  }
}

const openImage = async (item: any) => {
  imageDialog.value = true
  loadingImage.value = true
  currentImageUrl.value = null

  try {
    const blob = await RemoteService.getQuestionImage(item.id)
    currentImageUrl.value = window.URL.createObjectURL(blob)
  } catch (error) {
    console.error("Erro ao carregar a imagem:", error)
    alert("Não foi possível carregar a imagem da questão.")
  } finally {
    loadingImage.value = false
  }
}

const closeImage = () => {
  imageDialog.value = false
  if (currentImageUrl.value) {
    window.URL.revokeObjectURL(currentImageUrl.value)
    currentImageUrl.value = null
  }
}

const deleteItem = async (item: any) => {
  if (confirm(`Tem a certeza que deseja eliminar a Questão ${item.number} do Exame ${item.examId}?`)) {
    try {
      await RemoteService.deleteQuestion(item.id)
      await loadQuestions()
    } catch (error) {
      console.error("Erro ao eliminar questão:", error)
      alert("Erro ao eliminar a questão.")
    }
  }
}

const openInNewTab = () => {
  if (currentImageUrl.value) {
    window.open(currentImageUrl.value, '_blank')
  }
}

const openDistributeDialog = () => {
  targetSubjectCode.value = ''
  distributeDialog.value = true
}

const confirmDistribute = async () => {
  if (!targetSubjectCode.value.trim()) return

  distributeDialog.value = false
  try {
    await RemoteService.distributeQuestions(targetSubjectCode.value.trim())
    snackbar.value = {
      show: true,
      text: `Questões da disciplina ${targetSubjectCode.value} distribuídas com sucesso pelos professores!`,
      color: 'success'
    }
    await loadQuestions()
  } catch (error) {
    console.error("Erro ao distribuir questões:", error)
    snackbar.value = {
      show: true,
      text: "Erro ao realizar a distribuição. Verifica se existem professores associados à disciplina.",
      color: 'error'
    }
  }
}
</script>