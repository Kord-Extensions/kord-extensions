<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { Toggle, type ToggleEmits, type ToggleProps, useForwardPropsEmits } from "radix-vue"
	import { type ToggleVariants, toggleVariants } from "."
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<ToggleProps & {
		class?: HTMLAttributes["class"]
		variant?: ToggleVariants["variant"]
		size?: ToggleVariants["size"]
	}>(), {
		variant: "default",
		size: "default",
		disabled: false,
	})

	const emits = defineEmits<ToggleEmits>()

	const delegatedProps = computed(() => {
		const { class: _, size, variant, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<Toggle
		:class="cn(toggleVariants({ variant, size }), props.class)"
		v-bind="forwarded"
	>
		<slot />
	</Toggle>
</template>
