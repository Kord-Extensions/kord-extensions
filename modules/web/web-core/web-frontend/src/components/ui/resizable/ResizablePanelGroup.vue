<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { SplitterGroup, type SplitterGroupEmits, type SplitterGroupProps, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<SplitterGroupProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<SplitterGroupEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<SplitterGroup :class="cn('flex h-full w-full data-[panel-group-direction=vertical]:flex-col', props.class)"
								 v-bind="forwarded">
		<slot />
	</SplitterGroup>
</template>
