<template>
  <div>
    <v-menu location="bottom end" :close-on-content-click="false">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" @click="fetchNotifications" color="primary" variant="text" class="mr-2">
          <v-badge
            :content="unreadCount"
            :model-value="unreadCount > 0"
            color="error"
            dot
          >
            <v-icon>mdi-bell-outline</v-icon>
          </v-badge>
        </v-btn>
      </template>

      <v-card min-width="350" max-width="400" class="elevation-4">
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="text-subtitle-1 font-weight-bold">Notificações Recentes</v-toolbar-title>
          <v-spacer></v-spacer>
          <!-- Botão Marcar todas como lidas (visível apenas se houver não lidas) -->
          <v-tooltip location="top" v-if="unreadCount > 0">
            <template v-slot:activator="{ props }">
              <v-btn icon="mdi-check-all" variant="text" v-bind="props" @click="markAllAsRead"></v-btn>
            </template>
            <span>Marcar todas como lidas</span>
          </v-tooltip>
        </v-toolbar>

        <v-list max-height="300" class="overflow-y-auto pa-0">
          <v-list-item v-if="previewNotifications.length === 0" class="pa-4 text-center">
            <v-list-item-title class="text-grey text-body-2">
              Não tem notificações.
            </v-list-item-title>
          </v-list-item>

          <v-list-item
            v-for="notification in previewNotifications"
            :key="notification.id"
            :class="{ 'bg-blue-grey-lighten-5': !notification.read }"
            class="border-b"
            @click="markSingleAsRead(notification)"
          >
            <template v-slot:prepend>
              <v-icon size="small" :color="notification.read ? 'grey-lighten-1' : 'primary'" class="mr-3">
                {{ notification.read ? 'mdi-bell-check-outline' : 'mdi-bell-ring' }}
              </v-icon>
            </template>
            
            <v-list-item-title class="text-wrap text-body-2 font-weight-medium mb-1" style="line-height: 1.2;">
              {{ notification.message }}
            </v-list-item-title>
            
            <v-list-item-subtitle class="text-caption text-medium-emphasis">
              {{ formatTime(notification.createdAt) }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
        
        <v-divider></v-divider>
        <v-card-actions class="pa-0">
          <v-btn block variant="text" color="primary" @click="showAllDialog = true" class="text-none">
            Ver todas as notificações
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-menu>

    <v-dialog v-model="showAllDialog" max-width="600" scrollable>
      <v-card>
        <v-toolbar color="primary" density="compact">
          <v-toolbar-title class="font-weight-bold">Todas as Notificações</v-toolbar-title>
          <v-spacer></v-spacer>
          
          <v-btn 
            v-if="unreadCount > 0" 
            variant="text" 
            prepend-icon="mdi-check-all" 
            @click="markAllAsRead"
            class="mr-2 text-none"
          >
            Lidas
          </v-btn>

          <v-btn icon="mdi-close" variant="text" @click="showAllDialog = false"></v-btn>
        </v-toolbar>

        <v-card-text class="pa-0" style="max-height: 60vh;">
          <v-list class="pa-0">
            <v-list-item v-if="notifications.length === 0" class="pa-4 text-center">
              Ainda não tens histórico de notificações.
            </v-list-item>

            <v-list-item
              v-for="notification in notifications"
              :key="notification.id"
              :class="{ 'bg-blue-grey-lighten-5': !notification.read }"
              class="border-b"
              @click="markSingleAsRead(notification)"
            >
              <template v-slot:prepend>
                <v-icon size="small" :color="notification.read ? 'grey-lighten-1' : 'primary'" class="mr-3">
                  {{ notification.read ? 'mdi-bell-check-outline' : 'mdi-bell-ring' }}
                </v-icon>
              </template>
              
              <v-list-item-title class="text-wrap text-body-1 font-weight-medium mb-1">
                {{ notification.message }}
              </v-list-item-title>
              
              <v-list-item-subtitle class="text-caption text-medium-emphasis">
                {{ formatTime(notification.createdAt) }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import RemoteServices from '@/services/RemoteService'
import { useAuthStore } from '@/stores/auth'

const notifications = ref<any[]>([])
const authStore = useAuthStore()
const showAllDialog = ref(false)

const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

const previewNotifications = computed(() => {
  return notifications.value.slice(0, 5)
})

async function fetchNotifications() {
  if (!authStore.isAuthenticated) return
  
  try {
    const data = await RemoteServices.getNotifications()
    notifications.value = data || []
  } catch (error) {
    console.error('Erro ao carregar notificações:', error)
  }
}

async function markSingleAsRead(notification: any) {
  if (notification.read) return 
  
  try {
    await RemoteServices.markNotificationAsRead(notification.id)
    notification.read = true 
  } catch (error) {
    console.error('Erro ao marcar como lida:', error)
  }
}

async function markAllAsRead() {
  if (unreadCount.value === 0) return 
  
  try {
    await RemoteServices.markAllNotificationsAsRead()
    notifications.value.forEach(n => n.read = true)
  } catch (error) {
    console.error('Erro ao marcar todas como lidas:', error)
  }
}

function formatTime(isoString: string) {
  if (!isoString) return ''
  const d = new Date(isoString)
  return d.toLocaleString('pt-PT', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  fetchNotifications()
})
</script>