<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
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
