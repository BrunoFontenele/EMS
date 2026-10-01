<template>
  <v-container>
    <div class="d-flex align-center mb-6">
      <h2>Avaliação de Questões</h2>
      <v-spacer></v-spacer>
      <v-btn color="primary" prepend-icon="mdi-refresh" @click="loadCorrections">Atualizar</v-btn>
    </div>

    <v-card elevation="2">
      <v-data-table :headers="headers" :items="corrections" :loading="loading" hover>
        <template #item.score="{ item }">
          <v-chip :color="item.score !== null ? 'success' : 'warning'" size="small" class="font-weight-bold">
            {{ item.score !== null ? `${item.score} pts` : 'Por avaliar' }}
          </v-chip>
        </template>

        <template #item.maxScore="{ item }">
          <strong>{{ item.maxScore }} pts</strong>
        </template>

        <template #item.actions="{ item }">
          <div class="d-flex justify-end">
            <v-tooltip text="Ver Resposta (Imagem)" location="top">
              <template v-slot:activator="{ props }">
                <v-btn v-bind="props" icon="mdi-image" variant="text" size="small" color="info" @click="openImage(item)"></v-btn>
              </template>
            </v-tooltip>
            <v-tooltip text="Atribuir Nota" location="top">
              <template v-slot:activator="{ props }">
                <v-btn v-bind="props" icon="mdi-pencil" variant="text" size="small" color="primary" @click="openScoreDialog(item)"></v-btn>
              </template>
            </v-tooltip>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="scoreDialog" max-width="400px">
      <v-card>
        <v-toolbar color="primary" title="Atribuir Nota"></v-toolbar>
       <v-card-text class="pt-4">
          <p class="mb-4">A cotação máxima para esta questão é de <strong>{{ currentMaxScore }} pontos</strong>.</p>
          <v-text-field 
            v-model.number="currentScore" 
            :label="`Nota (0 - ${currentMaxScore})`" 
            type="number" 
            min="0" 
            :max="currentMaxScore"
            variant="outlined"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="scoreDialog = false">Cancelar</v-btn>
          <v-btn color="success" variant="flat" @click="saveScore">Guardar Avaliação</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="imageDialog" max-width="1000px">
      <v-card>
        <v-toolbar color="grey-darken-3" title="Visualização da Resposta"></v-toolbar>
        <v-card-text class="pa-4 text-center" style="background-color: #555; max-height: 75vh; overflow-y: auto;">
          <v-progress-circular v-if="loadingImage" indeterminate color="white"></v-progress-circular>
          <img v-else-if="currentImageUrl" :src="currentImageUrl" style="width: 100%; display: block; margin: 0 auto;" />
          <p v-else class="text-white mt-4">Erro a carregar a imagem.</p>
        </v-card-text>
        <v-card-actions>
          <v-btn color="info" @click="openInNewTab" v-if="currentImageUrl">Tamanho Real</v-btn>
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="closeImage">Fechar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="4000" location="top">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import RemoteService from '@/services/RemoteService'
import type CorrectionDto from '@/models/CorrectionDto'
import { useAuthStore } from '@/stores/auth' 

const authStore = useAuthStore() 

const corrections = ref<CorrectionDto[]>([])
const loading = ref(true)

const snackbar = ref({ show: false, text: '', color: 'success' })

const imageDialog = ref(false)
const loadingImage = ref(false)
const currentImageUrl = ref<string | null>(null)

const scoreDialog = ref(false)
const currentCorrection = ref<CorrectionDto | null>(null)
const currentScore = ref<number>(0)
const currentMaxScore = ref<number>(0)

const headers = [
  { title: 'ID Correção', key: 'id', align: 'start' as const },
  { title: 'ID Questão', key: 'questionId', align: 'center' as const },
  { title: 'Professor', key: 'professorEmail', align: 'center' as const },
  { title: 'Cotação Máxima', key: 'maxScore', align: 'center' as const }, 
  { title: 'Nota Atribuída', key: 'score', align: 'center' as const },
  { title: 'Ações', key: 'actions', sortable: false, align: 'end' as const }
]

onMounted(() => loadCorrections())

const loadCorrections = async () => {
  loading.value = true
  try {
    const userId = authStore.user?.id 
    const userRole = authStore.user?.role

    if (userRole === 'ADMINISTRATOR') {
      // Admin vê a tabela cheia (vai buscar tudo)
      corrections.value = await RemoteService.getCorrections()
    }
    else if (userId) {
      // 4. Usar a nova função do RemoteService passando o ID
      corrections.value = await RemoteService.getCorrections()
    } else {
      snackbar.value = { show: true, text: 'Sessão inválida. ID não encontrado.', color: 'error' }
    }
  } catch (error) {
    snackbar.value = { show: true, text: 'Erro ao carregar correções.', color: 'error' }
  } finally {
    loading.value = false
  }
}

const openScoreDialog = (item: any) => {
  currentCorrection.value = item
  currentScore.value = item.score !== null ? item.score : 0
  currentMaxScore.value = item.maxScore 
  scoreDialog.value = true
}

const saveScore = async () => {
  if (!currentCorrection.value) return

  if (currentScore.value < 0) {
    alert("A nota não pode ser negativa.")
    return
  }
  if (currentScore.value > currentMaxScore.value) { 
    alert(`A nota não pode ser superior à cotação máxima (${currentMaxScore.value} pts).`)
    return
  }
  
  try {
    const updatedDto: CorrectionDto = {
      ...currentCorrection.value,
      score: currentScore.value
    }

    await RemoteService.updateCorrectionScore(currentCorrection.value.id, updatedDto)
    
    scoreDialog.value = false
    snackbar.value = { show: true, text: 'Nota registada com sucesso!', color: 'success' }
    await loadCorrections()
  } catch (error) {
    snackbar.value = { show: true, text: 'Erro ao registar a nota.', color: 'error' }
  }
}

const openImage = async (item: any) => {
  imageDialog.value = true
  loadingImage.value = true
  currentImageUrl.value = null
  try {
    const blob = await RemoteService.getQuestionImage(item.questionId)
    currentImageUrl.value = window.URL.createObjectURL(blob)
  } catch (error) {
    snackbar.value = { show: true, text: 'Não foi possível carregar a imagem.', color: 'error' }
  } finally {
    loadingImage.value = false
  }
}

const closeImage = () => {
  imageDialog.value = false
  if (currentImageUrl.value) window.URL.revokeObjectURL(currentImageUrl.value)
}

const openInNewTab = () => {
  if (currentImageUrl.value) window.open(currentImageUrl.value, '_blank')
}
</script>