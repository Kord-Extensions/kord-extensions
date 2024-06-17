<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { TabsRoot, useForwardPropsEmits } from "radix-vue"
	import type { TabsRootEmits, TabsRootProps } from "radix-vue"

	const props = defineProps<TabsRootProps>()
	const emits = defineEmits<TabsRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<TabsRoot v-bind="forwarded">
		<slot />
	</TabsRoot>
</template>
