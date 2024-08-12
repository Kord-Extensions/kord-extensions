<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { MenubarSub, type MenubarSubEmits, useForwardPropsEmits } from "radix-vue"

	interface MenubarSubRootProps {
		defaultOpen?: boolean
		open?: boolean
	}

	const props = defineProps<MenubarSubRootProps>()
	const emits = defineEmits<MenubarSubEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<MenubarSub v-bind="forwarded">
		<slot />
	</MenubarSub>
</template>
