<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		RadioGroupIndicator,
		RadioGroupItem,
		type RadioGroupItemProps,
		useForwardProps,
	} from "radix-vue"
	import { Circle } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<RadioGroupItemProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<RadioGroupItem
		:class="
      cn(
        'aspect-square h-4 w-4 rounded-full border border-primary text-primary ring-offset-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
        props.class,
      )
    "
		v-bind="forwardedProps"
	>
		<RadioGroupIndicator
			class="flex items-center justify-center"
		>
			<Circle class="h-2.5 w-2.5 fill-current text-current" />
		</RadioGroupIndicator>
	</RadioGroupItem>
</template>
