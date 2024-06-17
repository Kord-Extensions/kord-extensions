<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { PaginationEllipsis, type PaginationEllipsisProps } from "radix-vue"
	import { MoreHorizontal } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<PaginationEllipsisProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<PaginationEllipsis :class="cn('w-9 h-9 flex items-center justify-center', props.class)" v-bind="delegatedProps">
		<slot>
			<MoreHorizontal />
		</slot>
	</PaginationEllipsis>
</template>
