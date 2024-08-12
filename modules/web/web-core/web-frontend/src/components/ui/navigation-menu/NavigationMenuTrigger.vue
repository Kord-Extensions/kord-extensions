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
		NavigationMenuTrigger,
		type NavigationMenuTriggerProps,
		useForwardProps,
	} from "radix-vue"
	import { ChevronDown } from "lucide-vue-next"
	import { navigationMenuTriggerStyle } from "."
	import { cn } from "@/lib/utils"

	const props = defineProps<NavigationMenuTriggerProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<NavigationMenuTrigger
		:class="cn(navigationMenuTriggerStyle(), 'group', props.class)"
		v-bind="forwardedProps"
	>
		<slot />
		<ChevronDown
			aria-hidden="true"
			class="relative top-px ml-1 h-3 w-3 transition duration-200 group-data-[state=open]:rotate-180"
		/>
	</NavigationMenuTrigger>
</template>
