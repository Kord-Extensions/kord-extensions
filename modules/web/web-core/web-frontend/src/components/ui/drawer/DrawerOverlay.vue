<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { DrawerOverlay } from "vaul-vue"
	import type { DialogOverlayProps } from "radix-vue"
	import { type HtmlHTMLAttributes, computed } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DialogOverlayProps & { class?: HtmlHTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<DrawerOverlay :class="cn('fixed inset-0 z-50 bg-black/80', props.class)" v-bind="delegatedProps" />
</template>
