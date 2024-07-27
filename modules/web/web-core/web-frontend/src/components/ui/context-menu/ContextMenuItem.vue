<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		ContextMenuItem,
		type ContextMenuItemEmits,
		type ContextMenuItemProps,
		useForwardPropsEmits,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ContextMenuItemProps & { class?: HTMLAttributes["class"], inset?: boolean }>()
	const emits = defineEmits<ContextMenuItemEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ContextMenuItem
		:class="cn(
      'relative flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
      inset && 'pl-8',
      props.class,
    )"
		v-bind="forwarded"
	>
		<slot />
	</ContextMenuItem>
</template>
