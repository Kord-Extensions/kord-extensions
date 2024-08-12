<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { DialogClose, type DialogCloseProps } from "radix-vue"

	const props = defineProps<DialogCloseProps>()
</script>

<template>
	<DialogClose v-bind="props">
		<slot />
	</DialogClose>
</template>
