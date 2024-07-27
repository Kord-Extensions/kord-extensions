<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import type { ComboboxGroupProps } from "radix-vue"
	import { ComboboxGroup, ComboboxLabel } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ComboboxGroupProps & {
		class?: HTMLAttributes["class"]
		heading?: string
	}>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ComboboxGroup
		:class="cn('overflow-hidden p-1 text-foreground [&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1.5 [&_[cmdk-group-heading]]:text-xs [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground', props.class)"
		v-bind="delegatedProps"
	>
		<ComboboxLabel v-if="heading" class="px-2 py-1.5 text-xs font-medium text-muted-foreground">
			{{ heading }}
		</ComboboxLabel>
		<slot />
	</ComboboxGroup>
</template>
