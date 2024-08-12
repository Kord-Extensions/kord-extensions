<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { ContextMenuTrigger, type ContextMenuTriggerProps, useForwardProps } from "radix-vue"

	const props = defineProps<ContextMenuTriggerProps>()

	const forwardedProps = useForwardProps(props)
</script>

<template>
	<ContextMenuTrigger v-bind="forwardedProps">
		<slot />
	</ContextMenuTrigger>
</template>
