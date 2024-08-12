<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { Primitive, type PrimitiveProps, useForwardProps } from "radix-vue"
	import { Dot } from "lucide-vue-next"

	const props = defineProps<PrimitiveProps>()
	const forwardedProps = useForwardProps(props)
</script>

<template>
	<Primitive v-bind="forwardedProps">
		<slot>
			<Dot />
		</slot>
	</primitive>
</template>
