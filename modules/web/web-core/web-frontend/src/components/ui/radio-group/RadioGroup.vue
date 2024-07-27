<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { RadioGroupRoot, type RadioGroupRootEmits, type RadioGroupRootProps, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<RadioGroupRootProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<RadioGroupRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<RadioGroupRoot
		:class="cn('grid gap-2', props.class)"
		v-bind="forwarded"
	>
		<slot />
	</RadioGroupRoot>
</template>
