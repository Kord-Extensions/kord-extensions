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
		ProgressIndicator,
		ProgressRoot,
		type ProgressRootProps,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(
		defineProps<ProgressRootProps & { class?: HTMLAttributes["class"] }>(),
		{
			modelValue: 0,
		},
	)

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ProgressRoot
		:class="
      cn(
        'relative h-4 w-full overflow-hidden rounded-full bg-secondary',
        props.class,
      )
    "
		v-bind="delegatedProps"
	>
		<ProgressIndicator
			:style="`transform: translateX(-${100 - (props.modelValue ?? 0)}%);`"
			class="h-full w-full flex-1 bg-primary transition-all"
		/>
	</ProgressRoot>
</template>
