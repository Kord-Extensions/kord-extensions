<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { computed } from "vue"
	import { beautifyObjectName } from "./utils"
	import type { FieldProps } from "./interface"
	import AutoFormLabel from "./AutoFormLabel.vue"
	import { FormControl, FormDescription, FormField, FormItem, FormMessage } from "@/components/ui/form"
	import { Switch } from "@/components/ui/switch"
	import { Checkbox } from "@/components/ui/checkbox"

	const props = defineProps<FieldProps>()

	const booleanComponent = computed(() => props.config?.component === "switch" ? Switch : Checkbox)
</script>

<template>
	<FormField v-slot="slotProps" :name="fieldName">
		<FormItem>
			<div class="space-y-0 mb-3 flex items-center gap-3">
				<FormControl>
					<slot v-bind="slotProps">
						<component
							:is="booleanComponent"
							:checked="slotProps.componentField.modelValue"
							:disabled="disabled"
							v-bind="{ ...slotProps.componentField }"
							@update:checked="slotProps.componentField['onUpdate:modelValue']"
						/>
					</slot>
				</FormControl>
				<AutoFormLabel v-if="!config?.hideLabel" :required="required">
					{{ config?.label || beautifyObjectName(label ?? fieldName) }}
				</AutoFormLabel>
			</div>

			<FormDescription v-if="config?.description">
				{{ config.description }}
			</FormDescription>
			<FormMessage />
		</FormItem>
	</FormField>
</template>
