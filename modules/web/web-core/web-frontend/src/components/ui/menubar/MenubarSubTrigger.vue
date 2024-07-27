<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { MenubarSubTrigger, type MenubarSubTriggerProps, useForwardProps } from "radix-vue"
	import { ChevronRight } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<MenubarSubTriggerProps & { class?: HTMLAttributes["class"], inset?: boolean }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<MenubarSubTrigger
		:class="cn(
      'flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[state=open]:bg-accent data-[state=open]:text-accent-foreground',
      inset && 'pl-8',
      props.class,
    )"
		v-bind="forwardedProps"
	>
		<slot />
		<ChevronRight class="ml-auto h-4 w-4" />
	</MenubarSubTrigger>
</template>
