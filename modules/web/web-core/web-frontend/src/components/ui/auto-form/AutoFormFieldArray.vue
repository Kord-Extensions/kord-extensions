<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script generic="T extends z.ZodAny" lang="ts" setup>
	import * as z from "zod"
	import { computed, provide } from "vue"
	import { PlusIcon, TrashIcon } from "lucide-vue-next"
	import { FieldArray, FieldContextKey, useField } from "vee-validate"
	import type { Config, ConfigItem } from "./interface"
	import { beautifyObjectName, getBaseType } from "./utils"
	import AutoFormField from "./AutoFormField.vue"
	import AutoFormLabel from "./AutoFormLabel.vue"
	import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
	import { Button } from "@/components/ui/button"
	import { Separator } from "@/components/ui/separator"
	import { FormItem, FormMessage } from "@/components/ui/form"

	const props = defineProps<{
		fieldName: string
		required?: boolean
		config?: Config<T>
		schema?: z.ZodArray<T>
		disabled?: boolean
	}>()

	function isZodArray(
		item: z.ZodArray<any> | z.ZodDefault<any>,
	): item is z.ZodArray<any> {
		return item instanceof z.ZodArray
	}

	function isZodDefault(
		item: z.ZodArray<any> | z.ZodDefault<any>,
	): item is z.ZodDefault<any> {
		return item instanceof z.ZodDefault
	}

	const itemShape = computed(() => {
		if (!props.schema)
			return

		const schema: z.ZodAny = isZodArray(props.schema)
			? props.schema._def.type
			: isZodDefault(props.schema)
				// @ts-expect-error missing schema
				? props.schema._def.innerType._def.type
				: null

		return {
			type: getBaseType(schema),
			schema,
		}
	})

	const fieldContext = useField(props.fieldName)
	// @ts-expect-error ignore missing `id`
	provide(FieldContextKey, fieldContext)
</script>

<template>
	<FieldArray v-slot="{ fields, remove, push }" :name="fieldName" as="section">
		<slot v-bind="props">
			<Accordion :disabled="disabled" as-child class="w-full" collapsible type="multiple">
				<FormItem>
					<AccordionItem :value="fieldName" class="border-none">
						<AccordionTrigger>
							<AutoFormLabel :required="required" class="text-base">
								{{ schema?.description || beautifyObjectName(fieldName) }}
							</AutoFormLabel>
						</AccordionTrigger>

						<AccordionContent>
							<template v-for="(field, index) of fields" :key="field.key">
								<div class="mb-4 p-1">
									<AutoFormField
										:config="config as ConfigItem"
										:field-name="`${fieldName}[${index}]`"
										:label="fieldName"
										:shape="itemShape!"
									/>

									<div class="!my-4 flex justify-end">
										<Button
											size="icon"
											type="button"
											variant="secondary"
											@click="remove(index)"
										>
											<TrashIcon :size="16" />
										</Button>
									</div>
									<Separator v-if="!field.isLast" />
								</div>
							</template>

							<Button
								class="mt-4 flex items-center"
								type="button"
								variant="secondary"
								@click="push(null)"
							>
								<PlusIcon :size="16" class="mr-2" />
								Add
							</Button>
						</AccordionContent>

						<FormMessage />
					</AccordionItem>
				</FormItem>
			</Accordion>
		</slot>
	</FieldArray>
</template>
