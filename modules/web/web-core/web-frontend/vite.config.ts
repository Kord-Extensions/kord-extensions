/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import path from "path"
import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"

import tailwind from "tailwindcss"
import autoprefixer from "autoprefixer"

// https://vitejs.dev/config/
export default defineConfig({
	css: {
		postcss: {
			plugins: [
				tailwind(),
				autoprefixer(),
			],
		},
	},

	plugins: [
		vue(),
	],

	resolve: {
		alias: {
			"@": path.resolve(__dirname, "./src"),
		},
	},

	build: {
		emptyOutDir: true,
		outDir: "dist/dev/kordex/modules/web/core/frontend",
	},

	server: {
		strictPort: true,
		port: 5173,
		open: false,
	},
})
