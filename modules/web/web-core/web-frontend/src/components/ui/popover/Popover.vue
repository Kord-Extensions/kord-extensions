<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { PopoverRoot, useForwardPropsEmits } from "radix-vue"
	import type { PopoverRootEmits, PopoverRootProps } from "radix-vue"

	const props = defineProps<PopoverRootProps>()
	const emits = defineEmits<PopoverRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<PopoverRoot v-bind="forwarded">
		<slot />
	</PopoverRoot>
</template>
