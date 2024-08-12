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
	import { type ButtonVariants, buttonVariants } from "."
	import { cn } from "@/lib/utils"

	interface Props extends PrimitiveProps {
		variant?: ButtonVariants["variant"]
		size?: ButtonVariants["size"]
		class?: HTMLAttributes["class"]
	}

	const props = withDefaults(defineProps<Props>(), {
		as: "button",
	})
</script>

<template>
	<Primitive
		:as="as"
		:as-child="asChild"
		:class="cn(buttonVariants({ variant, size }), props.class)"
	>
		<slot />
	</Primitive>
</template>
