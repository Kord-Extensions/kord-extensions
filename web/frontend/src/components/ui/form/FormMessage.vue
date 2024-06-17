<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
