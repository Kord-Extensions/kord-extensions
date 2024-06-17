<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		MenubarItemIndicator,
		MenubarRadioItem,
		type MenubarRadioItemEmits,
		type MenubarRadioItemProps,
		useForwardPropsEmits,
	} from "radix-vue"
	import { Circle } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<MenubarRadioItemProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<MenubarRadioItemEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<MenubarRadioItem
		:class="cn(
      'relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
      props.class,
    )"
		v-bind="forwarded"
	>
    <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <MenubarItemIndicator>
        <Circle class="h-2 w-2 fill-current" />
      </MenubarItemIndicator>
    </span>
		<slot />
	</MenubarRadioItem>
</template>
