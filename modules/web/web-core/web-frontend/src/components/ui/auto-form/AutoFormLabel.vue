<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { FormLabel } from "@/components/ui/form"

	defineProps<{
		required?: boolean
	}>()
</script>

<template>
	<FormLabel>
		<slot />
		<span v-if="required" class="text-destructive"> *</span>
	</FormLabel>
</template>
