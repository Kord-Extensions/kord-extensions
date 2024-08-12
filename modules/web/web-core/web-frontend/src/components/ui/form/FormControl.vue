<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { Slot } from "radix-vue"
	import { useFormField } from "./useFormField"

	const { error, formItemId, formDescriptionId, formMessageId } = useFormField()
</script>

<template>
	<Slot
		:id="formItemId"
		:aria-describedby="!error ? `${formDescriptionId}` : `${formDescriptionId} ${formMessageId}`"
		:aria-invalid="!!error"
	>
		<slot />
	</Slot>
</template>
