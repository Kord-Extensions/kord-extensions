<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts">
	import type { HTMLAttributes, InjectionKey } from "vue"

	export const FORM_ITEM_INJECTION_KEY
		= Symbol() as InjectionKey<string>
</script>

<script lang="ts" setup>
	import { provide } from "vue"
	import { useId } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()

	const id = useId()
	provide(FORM_ITEM_INJECTION_KEY, id)
</script>

<template>
	<div :class="cn('space-y-2', props.class)">
		<slot />
	</div>
</template>
