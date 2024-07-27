<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { computed } from "vue"
	import { ToastRoot, type ToastRootEmits, useForwardPropsEmits } from "radix-vue"
	import { type ToastProps, toastVariants } from "."
	import { cn } from "@/lib/utils"

	const props = defineProps<ToastProps>()

	const emits = defineEmits<ToastRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ToastRoot
		:class="cn(toastVariants({ variant }), props.class)"
		v-bind="forwarded"
		@update:open="onOpenChange"
	>
		<slot />
	</ToastRoot>
</template>
