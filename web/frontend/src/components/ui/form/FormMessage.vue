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
