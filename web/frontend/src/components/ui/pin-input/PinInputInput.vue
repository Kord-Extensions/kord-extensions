<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { PinInputInput, type PinInputInputProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<PinInputInputProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<PinInputInput :class="cn('relative text-center focus:outline-none focus:ring-2 focus:ring-ring focus:relative focus:z-10 flex h-10 w-10 items-center justify-center border-y border-r border-input text-sm transition-all first:rounded-l-md first:border-l last:rounded-r-md', props.class)"
								 v-bind="forwardedProps" />
</template>
