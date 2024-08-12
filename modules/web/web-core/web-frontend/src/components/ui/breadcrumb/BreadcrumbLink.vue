<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { Primitive, type PrimitiveProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<PrimitiveProps & { class?: HTMLAttributes["class"] }>(), {
		as: "a",
	})
</script>

<template>
	<Primitive
		:as="as"
		:as-child="asChild"
		:class="cn('transition-colors hover:text-foreground', props.class)"
	>
		<slot />
	</Primitive>
</template>
