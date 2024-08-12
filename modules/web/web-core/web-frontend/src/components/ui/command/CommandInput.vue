<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { Search } from "lucide-vue-next"
	import { ComboboxInput, type ComboboxInputProps, useForwardProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	defineOptions({
		inheritAttrs: false,
	})

	const props = defineProps<ComboboxInputProps & {
		class?: HTMLAttributes["class"]
	}>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<div class="flex items-center border-b px-3" cmdk-input-wrapper>
		<Search class="mr-2 h-4 w-4 shrink-0 opacity-50" />
		<ComboboxInput
			:class="cn('flex h-11 w-full rounded-md bg-transparent py-3 text-sm outline-none placeholder:text-muted-foreground disabled:cursor-not-allowed disabled:opacity-50', props.class)"
			v-bind="{ ...forwardedProps, ...$attrs }"

			auto-focus
		/>
	</div>
</template>
