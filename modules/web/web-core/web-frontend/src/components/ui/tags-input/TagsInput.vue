<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { TagsInputRoot, type TagsInputRootEmits, type TagsInputRootProps, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<TagsInputRootProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<TagsInputRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<TagsInputRoot :class="cn('flex flex-wrap gap-2 items-center rounded-md border border-input bg-background px-3 py-2 text-sm', props.class)"
								 v-bind="forwarded">
		<slot />
	</TagsInputRoot>
</template>
