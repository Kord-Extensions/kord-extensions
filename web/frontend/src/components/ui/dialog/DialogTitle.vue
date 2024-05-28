<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { DialogTitle, type DialogTitleProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DialogTitleProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<DialogTitle
		:class="
      cn(
        'text-lg font-semibold leading-none tracking-tight',
        props.class,
      )
    "
		v-bind="forwardedProps"
	>
		<slot />
	</DialogTitle>
</template>
