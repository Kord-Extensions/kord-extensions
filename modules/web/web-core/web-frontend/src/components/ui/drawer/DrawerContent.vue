<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { DrawerContent, DrawerPortal } from "vaul-vue"
	import type { DialogContentEmits, DialogContentProps } from "radix-vue"
	import { useForwardPropsEmits } from "radix-vue"
	import type { HtmlHTMLAttributes } from "vue"
	import DrawerOverlay from "./DrawerOverlay.vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DialogContentProps & { class?: HtmlHTMLAttributes["class"] }>()
	const emits = defineEmits<DialogContentEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<DrawerPortal>
		<DrawerOverlay />
		<DrawerContent
			:class="cn(
        'fixed inset-x-0 bottom-0 z-50 mt-24 flex h-auto flex-col rounded-t-[10px] border bg-background',
        props.class,
      )" v-bind="forwarded"
		>
			<div class="mx-auto mt-4 h-2 w-[100px] rounded-full bg-muted" />
			<slot />
		</DrawerContent>
	</DrawerPortal>
</template>
