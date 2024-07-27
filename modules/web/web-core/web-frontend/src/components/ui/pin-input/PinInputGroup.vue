<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { Primitive, type PrimitiveProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<PrimitiveProps & { class?: HTMLAttributes["class"] }>()
	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})
	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<Primitive :class="cn('flex items-center', props.class)" v-bind="forwardedProps">
		<slot />
	</primitive>
</template>
