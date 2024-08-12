<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { DateFormatter, getLocalTimeZone } from "@internationalized/date"
	import { CalendarIcon } from "lucide-vue-next"
	import { beautifyObjectName } from "./utils"
	import AutoFormLabel from "./AutoFormLabel.vue"
	import type { FieldProps } from "./interface"
	import { FormControl, FormDescription, FormField, FormItem, FormMessage } from "@/components/ui/form"

	import { Calendar } from "@/components/ui/calendar"
	import { Button } from "@/components/ui/button"
	import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
	import { cn } from "@/lib/utils"

	defineProps<FieldProps>()

	const df = new DateFormatter("en-US", {
		dateStyle: "long",
	})
</script>

<template>
	<FormField v-slot="slotProps" :name="fieldName">
		<FormItem>
			<AutoFormLabel v-if="!config?.hideLabel" :required="required">
				{{ config?.label || beautifyObjectName(label ?? fieldName) }}
			</AutoFormLabel>
			<FormControl>
				<slot v-bind="slotProps">
					<div>
						<Popover>
							<PopoverTrigger :disabled="disabled" as-child>
								<Button
									:class="cn(
                    'w-full justify-start text-left font-normal',
                    !slotProps.componentField.modelValue && 'text-muted-foreground',
                  )"
									variant="outline"
								>
									<CalendarIcon :size="16" class="mr-2 h-4 w-4" />
									{{ slotProps.componentField.modelValue ? df.format(slotProps.componentField.modelValue.toDate(getLocalTimeZone())) : "Pick a date"
									}}
								</Button>
							</PopoverTrigger>
							<PopoverContent class="w-auto p-0">
								<Calendar initial-focus v-bind="slotProps.componentField" />
							</PopoverContent>
						</Popover>
					</div>
				</slot>
			</FormControl>

			<FormDescription v-if="config?.description">
				{{ config.description }}
			</FormDescription>
			<FormMessage />
		</FormItem>
	</FormField>
</template>
