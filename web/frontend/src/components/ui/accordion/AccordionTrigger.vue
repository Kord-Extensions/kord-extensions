<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		AccordionHeader,
		AccordionTrigger,
		type AccordionTriggerProps,
	} from "radix-vue"
	import { ChevronDown } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<AccordionTriggerProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<AccordionHeader class="flex">
		<AccordionTrigger
			:class="
        cn(
          'flex flex-1 items-center justify-between py-4 font-medium transition-all hover:underline [&[data-state=open]>svg]:rotate-180',
          props.class,
        )
      "
			v-bind="delegatedProps"
		>
			<slot />
			<slot name="icon">
				<ChevronDown
					class="h-4 w-4 shrink-0 transition-transform duration-200"
				/>
			</slot>
		</AccordionTrigger>
	</AccordionHeader>
</template>
