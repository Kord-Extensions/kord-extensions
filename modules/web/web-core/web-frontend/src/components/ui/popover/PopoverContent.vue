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
		PopoverContent,
		type PopoverContentEmits,
		type PopoverContentProps,
		PopoverPortal,
		useForwardPropsEmits,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	defineOptions({
		inheritAttrs: false,
	})

	const props = withDefaults(
		defineProps<PopoverContentProps & { class?: HTMLAttributes["class"] }>(),
		{
			align: "center",
			sideOffset: 4,
		},
	)
	const emits = defineEmits<PopoverContentEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<PopoverPortal>
		<PopoverContent
			:class="
        cn(
          'z-50 w-72 rounded-md border bg-popover p-4 text-popover-foreground shadow-md outline-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2',
          props.class,
        )
      "
			v-bind="{ ...forwarded, ...$attrs }"
		>
			<slot />
		</PopoverContent>
	</PopoverPortal>
</template>
