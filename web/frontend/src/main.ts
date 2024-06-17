/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

// @ts-ignore
import * as Vue from "vue/dist/vue.esm-bundler.js"

import { createApp } from "vue"
import "./assets/index.css"
import App from "./App.vue"

// Apparently required for importing from external component files, if we decide to do that
// @ts-ignore
window.Vue = Vue

createApp(App).mount("#app")
