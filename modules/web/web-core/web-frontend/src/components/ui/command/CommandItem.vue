<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import type { ComboboxItemEmits, ComboboxItemProps } from "radix-vue"
	import { ComboboxItem, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ComboboxItemProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<ComboboxItemEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ComboboxItem
		:class="cn('relative flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none data-[highlighted]:bg-accent data-[disabled]:pointer-events-none data-[disabled]:opacity-50', props.class)"
		v-bind="forwarded"
	>
		<slot />
	</ComboboxItem>
</template>
