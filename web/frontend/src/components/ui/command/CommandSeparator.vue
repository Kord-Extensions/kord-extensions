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
