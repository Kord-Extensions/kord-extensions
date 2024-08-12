<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { ContextMenuPortal, type ContextMenuPortalProps } from "radix-vue"

	const props = defineProps<ContextMenuPortalProps>()
</script>

<template>
	<ContextMenuPortal v-bind="props">
		<slot />
	</ContextMenuPortal>
</template>
