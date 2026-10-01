<template>
  <v-app-bar color="secondary" :height="36" class="px-2">
    <div class="d-flex align-center w-100 h-100">

      <div class="d-flex justify-center align-center" style="flex: 1;">
        <v-btn 
          size="small" 
          variant="text" 
          :to="homeDestination" 
          class="font-weight-bold"
        >
          <v-icon size="small" class="me-1" icon="mdi-home"></v-icon>
          Gestão de Exames
        </v-btn>
      </div>

      <div class="d-flex justify-end align-center" style="flex: 1;">
        
        <!-- Impersonation -->
        <v-chip v-if="authStore.isImpersonating" color="warning" size="small" variant="flat" class="me-3">
          <v-icon start icon="mdi-account-switch" />
          A personificar {{ authStore.user?.email || authStore.user?.username || authStore.user?.name }}
          <v-btn size="x-small" variant="text" class="ms-1" @click="stopImpersonation">terminar</v-btn>
        </v-chip>

        <!-- Utilizador autenticado mostrando o Email -->
        <span v-if="authStore.user" class="text-caption">
          {{ authStore.user.email || authStore.user.username || authStore.user.name }} — {{ roleLabel(authStore.user.role) }}
        </span>

        <!-- Dark Mode -->
        <div class="ms-3 d-flex align-center">
          <DarkModeSwitch />
        </div>

        <!-- Login / Logout -->
        <div class="ms-2 d-flex align-center">
          <v-btn v-if="authStore.isAuthenticated" size="small" variant="text" @click="logout">
            Terminar sessão
            <v-icon size="small" class="ms-1" icon="mdi-logout"></v-icon>
          </v-btn>
          <v-btn v-else size="small" variant="text" to="/login">
            Iniciar sessão
            <v-icon size="small" class="ms-1" icon="mdi-login"></v-icon>
          </v-btn>
        </div>

      </div>

    </div>
  </v-app-bar>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DarkModeSwitch from './DarkModeSwitch.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const homeDestination = computed(() => {
  if (!authStore.isAuthenticated) {
    return '/'
  }

  if (authStore.user?.role === 'STUDENT') {
    return '/dashboard'
  }

  return '/dashboard'
})

const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATOR: 'Administrador',
  SCHOOL_STAFF: 'Funcionário Escolar',
  TEACHER: 'Professor',
  STUDENT: 'Aluno'
}
const roleLabel = (role: string) => ROLE_LABELS[role] ?? role

const logout = () => {
  authStore.logout()
  router.push({ name: 'home' })
}

const stopImpersonation = () => {
  authStore.stopImpersonation()
  router.push({ name: 'people' })
}
</script>
