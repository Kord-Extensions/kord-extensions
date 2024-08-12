<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script generic="U extends ZodAny" lang="ts" setup>
	import type { ZodAny } from "zod"
	import { computed } from "vue"
	import type { Config, ConfigItem, Shape } from "./interface"
	import { DEFAULT_ZOD_HANDLERS, INPUT_COMPONENTS } from "./constant"
	import useDependencies from "./dependencies"

	const props = defineProps<{
		fieldName: string
		shape: Shape
		config?: ConfigItem | Config<U>
	}>()

	function isValidConfig(config: any): config is ConfigItem {
		return !!config?.component
	}

	const delegatedProps = computed(() => {
		if (["ZodObject", "ZodArray"].includes(props.shape?.type))
			return { schema: props.shape?.schema }
		return undefined
	})

	const { isDisabled, isHidden, isRequired, overrideOptions } = useDependencies(props.fieldName)
</script>

<template>
	<component
		:is="isValidConfig(config)
      ? typeof config.component === 'string'
        ? INPUT_COMPONENTS[config.component!]
        : config.component
      : INPUT_COMPONENTS[DEFAULT_ZOD_HANDLERS[shape.type]] "
		v-if="!isHidden"
		:config="config"
		:disabled="isDisabled"
		:field-name="fieldName"
		:label="shape.schema?.description"
		:options="overrideOptions || shape.options"
		:required="isRequired || shape.required"
		v-bind="delegatedProps"
	>
		<slot />
	</component>
</template>
