<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import {
		ContextMenuRadioGroup,
		type ContextMenuRadioGroupEmits,
		type ContextMenuRadioGroupProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<ContextMenuRadioGroupProps>()
	const emits = defineEmits<ContextMenuRadioGroupEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<ContextMenuRadioGroup v-bind="forwarded">
		<slot />
	</ContextMenuRadioGroup>
</template>
