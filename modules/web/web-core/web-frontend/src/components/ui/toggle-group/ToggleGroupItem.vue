<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { VariantProps } from "class-variance-authority"
	import { type HTMLAttributes, computed, inject } from "vue"
	import { ToggleGroupItem, type ToggleGroupItemProps, useForwardProps } from "radix-vue"
	import { toggleVariants } from "@/components/ui/toggle"
	import { cn } from "@/lib/utils"

	type ToggleGroupVariants = VariantProps<typeof toggleVariants>

	const props = defineProps<ToggleGroupItemProps & {
		class?: HTMLAttributes["class"]
		variant?: ToggleGroupVariants["variant"]
		size?: ToggleGroupVariants["size"]
	}>()

	const context = inject<ToggleGroupVariants>("toggleGroup")

	const delegatedProps = computed(() => {
		const { class: _, variant, size, ...delegated } = props
		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<ToggleGroupItem
		:class="cn(toggleVariants({
      variant: context?.variant || variant,
      size: context?.size || size,
    }), props.class)" v-bind="forwardedProps"
	>
		<slot />
	</ToggleGroupItem>
</template>
