<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { ContextMenuRoot, useForwardPropsEmits } from "radix-vue"
	import type { ContextMenuRootEmits, ContextMenuRootProps } from "radix-vue"

	const props = defineProps<ContextMenuRootProps>()
	const emits = defineEmits<ContextMenuRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<ContextMenuRoot v-bind="forwarded">
		<slot />
	</ContextMenuRoot>
</template>
