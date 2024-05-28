<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { TabsList, type TabsListProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<TabsListProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<TabsList
		:class="cn(
      'inline-flex h-10 items-center justify-center rounded-md bg-muted p-1 text-muted-foreground',
      props.class,
    )"
		v-bind="delegatedProps"
	>
		<slot />
	</TabsList>
</template>
