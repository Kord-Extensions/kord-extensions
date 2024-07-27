<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { SelectScrollUpButton, type SelectScrollUpButtonProps, useForwardProps } from "radix-vue"
	import { ChevronUp } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectScrollUpButtonProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<SelectScrollUpButton :class="cn('flex cursor-default items-center justify-center py-1', props.class)"
												v-bind="forwardedProps">
		<slot>
			<ChevronUp class="h-4 w-4" />
		</slot>
	</SelectScrollUpButton>
</template>
