<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
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
