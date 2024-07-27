<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import {
		DropdownMenuRoot,
		type DropdownMenuRootEmits,
		type DropdownMenuRootProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<DropdownMenuRootProps>()
	const emits = defineEmits<DropdownMenuRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DropdownMenuRoot v-bind="forwarded">
		<slot />
	</DropdownMenuRoot>
</template>
