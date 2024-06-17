<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
