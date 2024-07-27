<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import type { LabelProps } from "radix-vue"
	import { useFormField } from "./useFormField"
	import { cn } from "@/lib/utils"
	import { Label } from "@/components/ui/label"

	const props = defineProps<LabelProps & { class?: HTMLAttributes["class"] }>()

	const { error, formItemId } = useFormField()
</script>

<template>
	<Label
		:class="cn(
      error && 'text-destructive',
      props.class,
    )"
		:for="formItemId"
	>
		<slot />
	</Label>
</template>
