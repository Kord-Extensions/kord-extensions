<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { Slot } from "radix-vue"
	import { useFormField } from "./useFormField"

	const { error, formItemId, formDescriptionId, formMessageId } = useFormField()
</script>

<template>
	<Slot
		:id="formItemId"
		:aria-describedby="!error ? `${formDescriptionId}` : `${formDescriptionId} ${formMessageId}`"
		:aria-invalid="!!error"
	>
		<slot />
	</Slot>
</template>
