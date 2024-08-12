<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { VariantProps } from "class-variance-authority"
	import { type HTMLAttributes, computed, provide } from "vue"
	import {
		ToggleGroupRoot,
		type ToggleGroupRootEmits,
		type ToggleGroupRootProps,
		useForwardPropsEmits,
	} from "radix-vue"
	import type { toggleVariants } from "@/components/ui/toggle"
	import { cn } from "@/lib/utils"

	type ToggleGroupVariants = VariantProps<typeof toggleVariants>

	const props = defineProps<ToggleGroupRootProps & {
		class?: HTMLAttributes["class"]
		variant?: ToggleGroupVariants["variant"]
		size?: ToggleGroupVariants["size"]
	}>()
	const emits = defineEmits<ToggleGroupRootEmits>()

	provide("toggleGroup", {
		variant: props.variant,
		size: props.size,
	})

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ToggleGroupRoot :class="cn('flex items-center justify-center gap-1', props.class)" v-bind="forwarded">
		<slot />
	</ToggleGroupRoot>
</template>
