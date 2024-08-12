<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import type { ComboboxGroupProps } from "radix-vue"
	import { ComboboxGroup, ComboboxLabel } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ComboboxGroupProps & {
		class?: HTMLAttributes["class"]
		heading?: string
	}>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ComboboxGroup
		:class="cn('overflow-hidden p-1 text-foreground [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground', props.class)"
		v-bind="delegatedProps"
	>
		<ComboboxLabel v-if="heading" class="px-2 py-1.5 text-xs font-medium text-muted-foreground">
			{{ heading }}
		</ComboboxLabel>
		<slot />
	</ComboboxGroup>
</template>
