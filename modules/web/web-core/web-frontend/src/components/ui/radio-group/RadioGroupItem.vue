<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		RadioGroupIndicator,
		RadioGroupItem,
		type RadioGroupItemProps,
		useForwardProps,
	} from "radix-vue"
	import { Circle } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<RadioGroupItemProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<RadioGroupItem
		:class="
      cn(
        'aspect-square h-4 w-4 rounded-full border border-primary text-primary ring-offset-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
        props.class,
      )
    "
		v-bind="forwardedProps"
	>
		<RadioGroupIndicator
			class="flex items-center justify-center"
		>
			<Circle class="h-2.5 w-2.5 fill-current text-current" />
		</RadioGroupIndicator>
	</RadioGroupItem>
</template>
