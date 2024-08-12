<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import type { LabelProps } from "radix-vue"
	import { useFormField } from "./useFormField"
	import { cn } from "@/lib/utils"
	import { Label } from "@/components/ui/label"

	const props = defineProps<LabelProps & { class?: HTMLAttributes["class"] }>()

	const { error, formItemId } = useFormField()
</script>

<template>
	<Label
		:class="cn(
      error && 'text-destructive',
      props.class,
    )"
		:for="formItemId"
	>
		<slot />
	</Label>
</template>
