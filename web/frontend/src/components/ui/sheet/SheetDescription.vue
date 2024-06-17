<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { DialogDescription, type DialogDescriptionProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DialogDescriptionProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<DialogDescription
		:class="cn('text-sm text-muted-foreground', props.class)"
		v-bind="delegatedProps"
	>
		<slot />
	</DialogDescription>
</template>
