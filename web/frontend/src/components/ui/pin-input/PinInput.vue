<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { PinInputRoot, type PinInputRootEmits, type PinInputRootProps, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<PinInputRootProps & { class?: HTMLAttributes["class"] }>(), {
		modelValue: () => [],
	})
	const emits = defineEmits<PinInputRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<PinInputRoot :class="cn('flex gap-2 items-center', props.class)" v-bind="forwarded">
		<slot />
	</PinInputRoot>
</template>
