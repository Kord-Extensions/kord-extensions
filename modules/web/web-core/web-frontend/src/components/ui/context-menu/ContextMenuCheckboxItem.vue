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
		ContextMenuCheckboxItem,
		type ContextMenuCheckboxItemEmits,
		type ContextMenuCheckboxItemProps,
		ContextMenuItemIndicator,
		useForwardPropsEmits,
	} from "radix-vue"
	import { Check } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<ContextMenuCheckboxItemProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<ContextMenuCheckboxItemEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<ContextMenuCheckboxItem
		:class="cn(
      'relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
      props.class,
    )"
		v-bind="forwarded"
	>
    <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <ContextMenuItemIndicator>
        <Check class="h-4 w-4" />
      </ContextMenuItemIndicator>
    </span>
		<slot />
	</ContextMenuCheckboxItem>
</template>
