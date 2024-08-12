<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import AutoFormLabel from "./AutoFormLabel.vue"
	import { beautifyObjectName } from "./utils"
	import type { FieldProps } from "./interface"
	import { FormControl, FormDescription, FormField, FormItem, FormMessage } from "@/components/ui/form"
	import { Input } from "@/components/ui/input"

	defineOptions({
		inheritAttrs: false,
	})

	defineProps<FieldProps>()
</script>

<template>
	<FormField v-slot="slotProps" :name="fieldName">
		<FormItem>
			<AutoFormLabel v-if="!config?.hideLabel" :required="required">
				{{ config?.label || beautifyObjectName(label ?? fieldName) }}
			</AutoFormLabel>
			<FormControl>
				<slot v-bind="slotProps">
					<Input :disabled="disabled" type="number" v-bind="{ ...slotProps.componentField, ...config?.inputProps }" />
				</slot>
			</FormControl>
			<FormDescription v-if="config?.description">
				{{ config.description }}
			</FormDescription>
			<FormMessage />
		</FormItem>
	</FormField>
</template>
