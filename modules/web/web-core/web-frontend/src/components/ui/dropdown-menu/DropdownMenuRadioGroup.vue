<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
