<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { SelectRootEmits, SelectRootProps } from "radix-vue"
	import { SelectRoot, useForwardPropsEmits } from "radix-vue"

	const props = defineProps<SelectRootProps>()
	const emits = defineEmits<SelectRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<SelectRoot v-bind="forwarded">
		<slot />
	</SelectRoot>
</template>
