<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
