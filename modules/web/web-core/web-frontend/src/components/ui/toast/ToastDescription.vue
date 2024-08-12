<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { ToastDescription, type ToastDescriptionProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ToastDescriptionProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ToastDescription :class="cn('text-sm opacity-90', props.class)" v-bind="delegatedProps">
		<slot />
	</ToastDescription>
</template>
