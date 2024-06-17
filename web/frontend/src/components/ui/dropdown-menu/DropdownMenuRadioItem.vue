<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		DropdownMenuItemIndicator,
		DropdownMenuRadioItem,
		type DropdownMenuRadioItemEmits,
		type DropdownMenuRadioItemProps,
		useForwardPropsEmits,
	} from "radix-vue"
	import { Circle } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<DropdownMenuRadioItemProps & { class?: HTMLAttributes["class"] }>()

	const emits = defineEmits<DropdownMenuRadioItemEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<DropdownMenuRadioItem
		:class="cn(
      'relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none transition-colors focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
      props.class,
    )"
		v-bind="forwarded"
	>
    <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <DropdownMenuItemIndicator>
        <Circle class="h-2 w-2 fill-current" />
      </DropdownMenuItemIndicator>
    </span>
		<slot />
	</DropdownMenuRadioItem>
</template>
