<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		ContextMenuContent,
		type ContextMenuContentEmits,
		type ContextMenuContentProps,
		ContextMenuPortal,
		useForwardPropsEmits,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ContextMenuContentProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<ContextMenuContentEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ContextMenuPortal>
		<ContextMenuContent
			:class="cn(
        'z-50 min-w-32 overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md animate-in fade-in-80 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2',
        props.class,
      )"
			v-bind="forwarded"
		>
			<slot />
		</ContextMenuContent>
	</ContextMenuPortal>
</template>
