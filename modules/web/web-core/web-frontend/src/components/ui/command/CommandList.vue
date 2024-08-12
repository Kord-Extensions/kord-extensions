<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import type { ComboboxContentEmits, ComboboxContentProps } from "radix-vue"
	import { ComboboxContent, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<ComboboxContentProps & { class?: HTMLAttributes["class"] }>(), {
		dismissable: false,
	})
	const emits = defineEmits<ComboboxContentEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ComboboxContent :class="cn('max-h-[300px] overflow-y-auto overflow-x-hidden', props.class)" v-bind="forwarded">
		<div role="presentation">
			<slot />
		</div>
	</ComboboxContent>
</template>
