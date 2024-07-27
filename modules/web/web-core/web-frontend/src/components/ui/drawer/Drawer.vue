<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { DrawerRootEmits, DrawerRootProps } from "vaul-vue"
	import { DrawerRoot } from "vaul-vue"
	import { useForwardPropsEmits } from "radix-vue"

	const props = withDefaults(defineProps<DrawerRootProps>(), {
		shouldScaleBackground: true,
	})

	const emits = defineEmits<DrawerRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DrawerRoot v-bind="forwarded">
		<slot />
	</DrawerRoot>
</template>
