<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { ErrorMessage } from "vee-validate"
	import { toValue } from "vue"
	import { useFormField } from "./useFormField"

	const { name, formMessageId } = useFormField()
</script>

<template>
	<ErrorMessage
		:id="formMessageId"
		:name="toValue(name)"
		as="p"
		class="text-sm font-medium text-destructive"
	/>
</template>
