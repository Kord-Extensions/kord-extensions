<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { TabsRoot, useForwardPropsEmits } from "radix-vue"
	import type { TabsRootEmits, TabsRootProps } from "radix-vue"

	const props = defineProps<TabsRootProps>()
	const emits = defineEmits<TabsRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<TabsRoot v-bind="forwarded">
		<slot />
	</TabsRoot>
</template>
