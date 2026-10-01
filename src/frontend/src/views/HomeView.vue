<template>
  <div class="home-container">
    <h1>Bem-vindo ao Sistema de Gestão de Exames</h1>
    
    <p>
      Este sistema permite aos professores e alunos gerirem as avaliações 
      de forma simples, incluindo a distribuição cega de pedidos de revisão.
    </p>

    <button @click="acessarSistema">
      Acessar
    </button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const acessarSistema = () => {
  if (auth.isAuthenticated) {
    router.push('/dashboard') 
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
.home-container {
  text-align: center;
  margin-top: 50px;
}

button {
  margin-top: 20px;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
}
</style>