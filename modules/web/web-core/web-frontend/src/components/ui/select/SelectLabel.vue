<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { SelectLabel, type SelectLabelProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectLabelProps & { class?: HTMLAttributes["class"] }>()
</script>

<template>
	<SelectLabel :class="cn('py-1.5 pl-8 pr-2 text-sm font-semibold', props.class)">
		<slot />
	</SelectLabel>
</template>
