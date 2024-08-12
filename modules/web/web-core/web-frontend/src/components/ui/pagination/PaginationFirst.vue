<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { PaginationFirst, type PaginationFirstProps } from "radix-vue"
	import { ChevronsLeft } from "lucide-vue-next"
	import {
		Button,
	} from "@/components/ui/button"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<PaginationFirstProps & { class?: HTMLAttributes["class"] }>(), {
		asChild: true,
	})

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<PaginationFirst v-bind="delegatedProps">
		<Button :class="cn('w-10 h-10 p-0', props.class)" variant="outline">
			<slot>
				<ChevronsLeft class="h-4 w-4" />
			</slot>
		</Button>
	</PaginationFirst>
</template>
