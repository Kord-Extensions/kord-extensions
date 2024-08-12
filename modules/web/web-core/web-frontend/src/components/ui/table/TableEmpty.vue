<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import TableRow from "./TableRow.vue"
	import TableCell from "./TableCell.vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<{
		class?: HTMLAttributes["class"]
		colspan?: number
	}>(), {
		colspan: 1,
	})

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<TableRow>
		<TableCell
			:class="
        cn(
          'p-4 whitespace-nowrap align-middle text-sm text-foreground',
          props.class,
        )
      "
			v-bind="delegatedProps"
		>
			<div class="flex items-center justify-center py-10">
				<slot />
			</div>
		</TableCell>
	</TableRow>
</template>
