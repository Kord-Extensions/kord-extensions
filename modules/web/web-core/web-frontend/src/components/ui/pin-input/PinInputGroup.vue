<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { Primitive, type PrimitiveProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<PrimitiveProps & { class?: HTMLAttributes["class"] }>()
	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})
	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<Primitive :class="cn('flex items-center', props.class)" v-bind="forwardedProps">
		<slot />
	</primitive>
</template>
