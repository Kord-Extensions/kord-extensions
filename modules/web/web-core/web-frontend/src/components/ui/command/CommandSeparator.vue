<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import type { ComboboxSeparatorProps } from "radix-vue"
	import { ComboboxSeparator } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ComboboxSeparatorProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ComboboxSeparator
		:class="cn('-mx-1 h-px bg-border', props.class)"
		v-bind="delegatedProps"
	>
		<slot />
	</ComboboxSeparator>
</template>
