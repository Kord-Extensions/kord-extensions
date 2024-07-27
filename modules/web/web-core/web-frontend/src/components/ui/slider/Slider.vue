<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import type { SliderRootEmits, SliderRootProps } from "radix-vue"
	import { SliderRange, SliderRoot, SliderThumb, SliderTrack, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<SliderRootProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<SliderRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<SliderRoot
		:class="cn(
      'relative flex w-full touch-none select-none items-center',
      props.class,
    )"
		v-bind="forwarded"
	>
		<SliderTrack class="relative h-2 w-full grow overflow-hidden rounded-full bg-secondary">
			<SliderRange class="absolute h-full bg-primary" />
		</SliderTrack>
		<SliderThumb
			v-for="(_, key) in modelValue"
			:key="key"
			class="block h-5 w-5 rounded-full border-2 border-primary bg-background ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50"
		/>
	</SliderRoot>
</template>
