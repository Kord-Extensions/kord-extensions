// @ts-ignore
import * as Vue from "vue/dist/vue.esm-bundler.js"

import { createApp } from "vue"
import "./assets/index.css"
import App from "./App.vue"

// Apparently required for importing from external component files, if we decide to do that
// @ts-ignore
window.Vue = Vue

createApp(App).mount("#app")
