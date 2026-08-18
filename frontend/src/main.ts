import { createApp } from 'vue'

import App from './App.vue'
import router from './router'

// Pinia was scaffolded in but never used — the only store was the generated
// counter example. Dropped rather than shipped as dead weight.
createApp(App).use(router).mount('#app')
