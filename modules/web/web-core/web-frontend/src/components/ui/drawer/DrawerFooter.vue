<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HtmlHTMLAttributes } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HtmlHTMLAttributes["class"]
	}>()
</script>

<template>
	<div :class="cn('mt-auto flex flex-col gap-2 p-4', props.class)">
		<slot />
	</div>
</template>
