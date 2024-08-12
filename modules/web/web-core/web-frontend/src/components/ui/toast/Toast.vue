<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
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
