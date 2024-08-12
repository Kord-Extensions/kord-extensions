<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { DrawerRootEmits, DrawerRootProps } from "vaul-vue"
	import { DrawerRoot } from "vaul-vue"
	import { useForwardPropsEmits } from "radix-vue"

	const props = withDefaults(defineProps<DrawerRootProps>(), {
		shouldScaleBackground: true,
	})

	const emits = defineEmits<DrawerRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DrawerRoot v-bind="forwarded">
		<slot />
	</DrawerRoot>
</template>
