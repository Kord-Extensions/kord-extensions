<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { RangeCalendarGridBody, type RangeCalendarGridBodyProps } from "radix-vue"

	const props = defineProps<RangeCalendarGridBodyProps>()
</script>

<template>
	<RangeCalendarGridBody v-bind="props">
		<slot />
	</RangeCalendarGridBody>
</template>
