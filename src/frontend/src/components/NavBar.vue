<template>
  <v-app-bar>
    <v-spacer></v-spacer>
    <v-btn v-for="item in navbarItems" :key="item.name" :to="item.path">
      {{ item.name }}
      <v-icon color="contrast" :icon="item.icon"></v-icon>
    </v-btn>

    <!-- Sino de Notificações -->
    <NotificationBell v-if="authStore.isAuthenticated" class="ml-2 mr-2" />
  </v-app-bar>
</template>

<script setup lang="ts">
import NotificationBell from '@/components/NotificationBell.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

defineProps<{ navbarItems: { name: string; path: string; icon: string }[] }>()
</script>