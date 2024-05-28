import path from "path"
import {defineConfig} from "vite"
import vue from "@vitejs/plugin-vue"

import tailwind from "tailwindcss"
import autoprefixer from "autoprefixer"

// https://vitejs.dev/config/
export default defineConfig({
	css: {
		postcss: {
			plugins: [tailwind(), autoprefixer()],
		},
	},

	plugins: [vue()],

	resolve: {
		alias: {
			"@": path.resolve(__dirname, "./src"),
		},
	},
})
