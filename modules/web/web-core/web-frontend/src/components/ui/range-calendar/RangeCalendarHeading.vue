<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { RangeCalendarHeading, type RangeCalendarHeadingProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<RangeCalendarHeadingProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<RangeCalendarHeading
		v-slot="{ headingValue }"
		:class="cn('text-sm font-medium', props.class)"
		v-bind="forwardedProps"
	>
		<slot :heading-value>
			{{ headingValue }}
		</slot>
	</RangeCalendarHeading>
</template>
