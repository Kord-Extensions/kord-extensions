<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { DialogRoot, type DialogRootEmits, type DialogRootProps, useForwardPropsEmits } from "radix-vue"

	const props = defineProps<DialogRootProps>()
	const emits = defineEmits<DialogRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DialogRoot v-bind="forwarded">
		<slot />
	</DialogRoot>
</template>
