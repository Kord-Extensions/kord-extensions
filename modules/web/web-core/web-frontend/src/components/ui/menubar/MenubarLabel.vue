<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { MenubarLabel, type MenubarLabelProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<MenubarLabelProps & { class?: HTMLAttributes["class"], inset?: boolean }>()
</script>

<template>
	<MenubarLabel :class="cn('px-2 py-1.5 text-sm font-semibold', inset && 'pl-8', props.class)">
		<slot />
	</MenubarLabel>
</template>
