<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { computed } from "vue"
	import AutoFormLabel from "./AutoFormLabel.vue"
	import { beautifyObjectName } from "./utils"
	import type { FieldProps } from "./interface"
	import { FormControl, FormDescription, FormField, FormItem, FormMessage } from "@/components/ui/form"
	import { Input } from "@/components/ui/input"
	import { Textarea } from "@/components/ui/textarea"

	const props = defineProps<FieldProps>()
	const inputComponent = computed(() => props.config?.component === "textarea" ? Textarea : Input)
</script>

<template>
	<FormField v-slot="slotProps" :name="fieldName">
		<FormItem v-bind="$attrs">
			<AutoFormLabel v-if="!config?.hideLabel" :required="required">
				{{ config?.label || beautifyObjectName(label ?? fieldName) }}
			</AutoFormLabel>
			<FormControl>
				<slot v-bind="slotProps">
					<component
						:is="inputComponent"
						:disabled="disabled"
						type="text"
						v-bind="{ ...slotProps.componentField, ...config?.inputProps }"
					/>
				</slot>
			</FormControl>
			<FormDescription v-if="config?.description">
				{{ config.description }}
			</FormDescription>
			<FormMessage />
		</FormItem>
	</FormField>
</template>
