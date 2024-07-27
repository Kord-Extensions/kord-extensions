<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import {
		DropdownMenuSub,
		type DropdownMenuSubEmits,
		type DropdownMenuSubProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<DropdownMenuSubProps>()
	const emits = defineEmits<DropdownMenuSubEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DropdownMenuSub v-bind="forwarded">
		<slot />
	</DropdownMenuSub>
</template>
