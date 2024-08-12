<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts">
	import type { HTMLAttributes, InjectionKey } from "vue"

	export const FORM_ITEM_INJECTION_KEY
		= Symbol() as InjectionKey<string>
</script>

<script lang="ts" setup>
	import { provide } from "vue"
	import { useId } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()

	const id = useId()
	provide(FORM_ITEM_INJECTION_KEY, id)
</script>

<template>
	<div :class="cn('space-y-2', props.class)">
		<slot />
	</div>
</template>
