<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { ScrollAreaScrollbar, type ScrollAreaScrollbarProps, ScrollAreaThumb } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<ScrollAreaScrollbarProps & { class?: HTMLAttributes["class"] }>(), {
		orientation: "vertical",
	})

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ScrollAreaScrollbar
		:class="
      cn('flex touch-none select-none transition-colors',
         orientation === 'vertical'
           && 'h-full w-2.5 border-l border-l-transparent p-px',
         orientation === 'horizontal'
           && 'h-2.5 flex-col border-t border-t-transparent p-px',
         props.class)"
		v-bind="delegatedProps"
	>
		<ScrollAreaThumb class="relative flex-1 rounded-full bg-border" />
	</ScrollAreaScrollbar>
</template>
