<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		ContextMenuSeparator,
		type ContextMenuSeparatorProps,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ContextMenuSeparatorProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ContextMenuSeparator :class="cn('-mx-1 my-1 h-px bg-border', props.class)" v-bind="delegatedProps" />
</template>
