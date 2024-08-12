<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { CollapsibleContent, type CollapsibleContentProps } from "radix-vue"

	const props = defineProps<CollapsibleContentProps>()
</script>

<template>
	<CollapsibleContent class="overflow-hidden transition-all data-[state=closed]:animate-collapsible-up data-[state=open]:animate-collapsible-down"
											v-bind="props">
		<slot />
	</CollapsibleContent>
</template>
