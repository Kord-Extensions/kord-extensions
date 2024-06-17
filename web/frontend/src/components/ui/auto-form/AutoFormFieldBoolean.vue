<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
