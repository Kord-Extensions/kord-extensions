<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import {
		MenubarRadioGroup,
		type MenubarRadioGroupEmits,
		type MenubarRadioGroupProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<MenubarRadioGroupProps>()

	const emits = defineEmits<MenubarRadioGroupEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<MenubarRadioGroup v-bind="forwarded">
		<slot />
	</MenubarRadioGroup>
</template>
