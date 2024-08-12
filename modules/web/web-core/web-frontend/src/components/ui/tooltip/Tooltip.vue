<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { TooltipRoot, type TooltipRootEmits, type TooltipRootProps, useForwardPropsEmits } from "radix-vue"

	const props = defineProps<TooltipRootProps>()
	const emits = defineEmits<TooltipRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<TooltipRoot v-bind="forwarded">
		<slot />
	</TooltipRoot>
</template>
