<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { AvatarRoot } from "radix-vue"
	import { type AvatarVariants, avatarVariant } from "."
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<{
		class?: HTMLAttributes["class"]
		size?: AvatarVariants["size"]
		shape?: AvatarVariants["shape"]
	}>(), {
		size: "sm",
		shape: "circle",
	})
</script>

<template>
	<AvatarRoot :class="cn(avatarVariant({ size, shape }), props.class)">
		<slot />
	</AvatarRoot>
</template>
