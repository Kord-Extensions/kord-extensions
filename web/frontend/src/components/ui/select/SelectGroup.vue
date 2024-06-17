<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { SelectGroup, type SelectGroupProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectGroupProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<SelectGroup :class="cn('p-1 w-full', props.class)" v-bind="delegatedProps">
		<slot />
	</SelectGroup>
</template>
