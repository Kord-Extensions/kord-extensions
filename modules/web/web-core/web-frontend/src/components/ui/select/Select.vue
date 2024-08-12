<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { SelectRootEmits, SelectRootProps } from "radix-vue"
	import { SelectRoot, useForwardPropsEmits } from "radix-vue"

	const props = defineProps<SelectRootProps>()
	const emits = defineEmits<SelectRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<SelectRoot v-bind="forwarded">
		<slot />
	</SelectRoot>
</template>
