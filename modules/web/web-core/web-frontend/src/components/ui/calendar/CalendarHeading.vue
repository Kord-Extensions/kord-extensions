<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { CalendarHeading, type CalendarHeadingProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<CalendarHeadingProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<CalendarHeading
		v-slot="{ headingValue }"
		:class="cn('text-sm font-medium', props.class)"
		v-bind="forwardedProps"
	>
		<slot :heading-value>
			{{ headingValue }}
		</slot>
	</CalendarHeading>
</template>
