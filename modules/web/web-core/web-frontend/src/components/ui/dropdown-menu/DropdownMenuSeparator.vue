<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		DropdownMenuSeparator,
		type DropdownMenuSeparatorProps,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DropdownMenuSeparatorProps & {
		class?: HTMLAttributes["class"]
	}>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<DropdownMenuSeparator :class="cn('-mx-1 my-1 h-px bg-muted', props.class)" v-bind="delegatedProps" />
</template>
