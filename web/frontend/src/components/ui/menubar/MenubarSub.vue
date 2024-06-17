<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { MenubarSub, type MenubarSubEmits, useForwardPropsEmits } from "radix-vue"

	interface MenubarSubRootProps {
		defaultOpen?: boolean
		open?: boolean
	}

	const props = defineProps<MenubarSubRootProps>()
	const emits = defineEmits<MenubarSubEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<MenubarSub v-bind="forwarded">
		<slot />
	</MenubarSub>
</template>
