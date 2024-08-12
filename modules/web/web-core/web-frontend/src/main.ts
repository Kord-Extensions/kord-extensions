/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

// @ts-ignore
import * as Vue from "vue/dist/vue.esm-bundler.js"

import { createApp } from "vue"
import { createHead } from '@unhead/vue'

import "./assets/index.css"
import App from "./App.vue"

// Apparently required for importing from external component files, if we decide to do that
// @ts-ignore
window.Vue = Vue

const app = createApp(App)
const head = createHead()

app.use(head)
app.mount("#app")
