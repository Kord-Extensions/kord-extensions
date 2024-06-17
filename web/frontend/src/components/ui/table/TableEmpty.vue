<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
