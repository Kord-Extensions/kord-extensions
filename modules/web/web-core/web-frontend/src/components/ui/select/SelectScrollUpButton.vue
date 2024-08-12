<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { SelectScrollUpButton, type SelectScrollUpButtonProps, useForwardProps } from "radix-vue"
	import { ChevronUp } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectScrollUpButtonProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<SelectScrollUpButton :class="cn('flex cursor-default items-center justify-center py-1', props.class)"
												v-bind="forwardedProps">
		<slot>
			<ChevronUp class="h-4 w-4" />
		</slot>
	</SelectScrollUpButton>
</template>
