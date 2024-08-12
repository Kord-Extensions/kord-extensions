<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { useFormField } from "./useFormField"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()

	const { formDescriptionId } = useFormField()
</script>

<template>
	<p
		:id="formDescriptionId"
		:class="cn('text-sm text-muted-foreground', props.class)"
	>
		<slot />
	</p>
</template>
