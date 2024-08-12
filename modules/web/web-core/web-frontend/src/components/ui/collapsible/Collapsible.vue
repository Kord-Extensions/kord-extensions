<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { CollapsibleRoot, useForwardPropsEmits } from "radix-vue"
	import type { CollapsibleRootEmits, CollapsibleRootProps } from "radix-vue"

	const props = defineProps<CollapsibleRootProps>()
	const emits = defineEmits<CollapsibleRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<CollapsibleRoot v-slot="{ open }" v-bind="forwarded">
		<slot :open="open" />
	</CollapsibleRoot>
</template>
