<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { TooltipTrigger, type TooltipTriggerProps } from "radix-vue"

	const props = defineProps<TooltipTriggerProps>()
</script>

<template>
	<TooltipTrigger v-bind="props">
		<slot />
	</TooltipTrigger>
</template>
