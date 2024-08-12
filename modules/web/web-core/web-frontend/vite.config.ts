/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
