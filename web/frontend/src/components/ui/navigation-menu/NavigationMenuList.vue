<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { NavigationMenuList, type NavigationMenuListProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<NavigationMenuListProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<NavigationMenuList
		:class="
      cn(
        'group flex flex-1 list-none items-center justify-center gap-x-1',
        props.class,
      )
    "
		v-bind="forwardedProps"
	>
		<slot />
	</NavigationMenuList>
</template>
