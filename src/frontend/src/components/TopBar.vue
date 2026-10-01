<template>
  <div>
    <UtilBar />
    <NavBar v-if="authStore.isAuthenticated" :navbarItems="navbarItems" />
  </div>
</template>

<script setup lang="ts">
import UtilBar from '@/components/UtilBar.vue'
import NavBar from '@/components/NavBar.vue'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const authStore = useAuthStore()

const navbarItems = computed(() => {
  const role = authStore.user?.role

  if (role === 'ADMINISTRATOR') {
    return [
      { name: 'Pessoal', path: '/people', icon: 'mdi-account-group' },
      { name: 'Escolas', path: '/schools', icon: 'mdi-domain' },
      { name: 'Disciplinas', path: '/subjects', icon: 'mdi-book-multiple' },
      { name: 'Exames', path: '/exams', icon: 'mdi-file-document-edit' },
      { name: 'Questões', path: '/questions', icon: 'mdi-image-multiple' },
      { name: 'Correções', path: '/corrections', icon: 'mdi-check-decagram' },
      { name: 'Revisões', path: '/reviews', icon: 'mdi-message-alert' },
      { name: 'Registo de Auditoria', path: '/audit-logs', icon: 'mdi-file-document-outline' }
    ]
  }

  if (role === 'TEACHER') {
    return [
      { name: 'Correções', path: '/corrections', icon: 'mdi-check-decagram' },
      { name: 'Revisões', path: '/reviews', icon: 'mdi-message-alert' }
    ]
  }

  if (role === 'SCHOOL_STAFF') {
    return [
      { name: 'Exames', path: '/exams', icon: 'mdi-file-document-edit' },
      { name: 'Questões', path: '/questions', icon: 'mdi-image-multiple' },
      { name: 'Estatísticas', path: '/statistics', icon: 'mdi-chart-bar' }
    ]
  }

  if (role === 'STUDENT') {
    return [
      { name: 'Os Meus Exames', path: '/student/dashboard', icon: 'mdi-school' }
    ]
  }

  return []
})
</script>