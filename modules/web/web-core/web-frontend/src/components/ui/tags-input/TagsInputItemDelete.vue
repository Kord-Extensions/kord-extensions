<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { TagsInputItemDelete, type TagsInputItemDeleteProps, useForwardProps } from "radix-vue"
	import { X } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<TagsInputItemDeleteProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<TagsInputItemDelete :class="cn('flex rounded bg-transparent mr-1', props.class)" v-bind="forwardedProps">
		<slot>
			<X class="w-4 h-4" />
		</slot>
	</TagsInputItemDelete>
</template>
