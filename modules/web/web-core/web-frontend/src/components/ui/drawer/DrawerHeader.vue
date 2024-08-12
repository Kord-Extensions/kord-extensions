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
	<div :class="cn('grid gap-1.5 p-4 text-center sm:text-left', props.class)">
		<slot />
	</div>
</template>
