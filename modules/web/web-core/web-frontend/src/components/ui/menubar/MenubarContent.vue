<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		MenubarContent,
		type MenubarContentProps,
		MenubarPortal,
		useForwardProps,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(
		defineProps<MenubarContentProps & { class?: HTMLAttributes["class"] }>(),
		{
			align: "start",
			alignOffset: -4,
			sideOffset: 8,
		},
	)

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<MenubarPortal>
		<MenubarContent
			:class="
        cn(
          'z-50 min-w-48 overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2',
          props.class,
        )
      "
			v-bind="forwardedProps"
		>
			<slot />
		</MenubarContent>
	</MenubarPortal>
</template>
