<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { ToastTitle, type ToastTitleProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ToastTitleProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ToastTitle :class="cn('text-sm font-semibold', props.class)" v-bind="delegatedProps">
		<slot />
	</ToastTitle>
</template>
