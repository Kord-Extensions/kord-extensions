<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { CollapsibleRoot, useForwardPropsEmits } from "radix-vue"
	import type { CollapsibleRootEmits, CollapsibleRootProps } from "radix-vue"

	const props = defineProps<CollapsibleRootProps>()
	const emits = defineEmits<CollapsibleRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<CollapsibleRoot v-slot="{ open }" v-bind="forwarded">
		<slot :open="open" />
	</CollapsibleRoot>
</template>
