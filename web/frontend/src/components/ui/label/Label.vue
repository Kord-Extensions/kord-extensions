<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { Label, type LabelProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<LabelProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<Label
		:class="
      cn(
        'text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70',
        props.class,
      )
    "
		v-bind="delegatedProps"
	>
		<slot />
	</Label>
</template>
