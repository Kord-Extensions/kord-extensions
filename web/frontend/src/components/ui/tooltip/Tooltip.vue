<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { TooltipRoot, type TooltipRootEmits, type TooltipRootProps, useForwardPropsEmits } from "radix-vue"

	const props = defineProps<TooltipRootProps>()
	const emits = defineEmits<TooltipRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<TooltipRoot v-bind="forwarded">
		<slot />
	</TooltipRoot>
</template>
