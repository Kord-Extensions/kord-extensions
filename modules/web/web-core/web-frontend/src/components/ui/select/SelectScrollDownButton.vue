<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { SelectScrollDownButton, type SelectScrollDownButtonProps, useForwardProps } from "radix-vue"
	import { ChevronDown } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectScrollDownButtonProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<SelectScrollDownButton :class="cn('flex cursor-default items-center justify-center py-1', props.class)"
													v-bind="forwardedProps">
		<slot>
			<ChevronDown class="h-4 w-4" />
		</slot>
	</SelectScrollDownButton>
</template>
