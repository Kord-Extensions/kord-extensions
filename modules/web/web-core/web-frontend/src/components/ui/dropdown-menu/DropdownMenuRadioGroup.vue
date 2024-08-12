<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import {
		DropdownMenuRadioGroup,
		type DropdownMenuRadioGroupEmits,
		type DropdownMenuRadioGroupProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<DropdownMenuRadioGroupProps>()
	const emits = defineEmits<DropdownMenuRadioGroupEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DropdownMenuRadioGroup v-bind="forwarded">
		<slot />
	</DropdownMenuRadioGroup>
</template>
