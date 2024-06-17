<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { SelectIcon, SelectTrigger, type SelectTriggerProps, useForwardProps } from "radix-vue"
	import { ChevronDown } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectTriggerProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<SelectTrigger
		:class="cn(
      'flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 [&>span]:line-clamp-1',
      props.class,
    )"
		v-bind="forwardedProps"
	>
		<slot />
		<SelectIcon as-child>
			<ChevronDown class="w-4 h-4 opacity-50" />
		</SelectIcon>
	</SelectTrigger>
</template>
