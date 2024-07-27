<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { DrawerOverlay } from "vaul-vue"
	import type { DialogOverlayProps } from "radix-vue"
	import { type HtmlHTMLAttributes, computed } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DialogOverlayProps & { class?: HtmlHTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<DrawerOverlay :class="cn('fixed inset-0 z-50 bg-black/80', props.class)" v-bind="delegatedProps" />
</template>
