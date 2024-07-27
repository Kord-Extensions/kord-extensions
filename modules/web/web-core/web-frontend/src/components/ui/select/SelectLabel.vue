<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { SelectLabel, type SelectLabelProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<SelectLabelProps & { class?: HTMLAttributes["class"] }>()
</script>

<template>
	<SelectLabel :class="cn('py-1.5 pl-8 pr-2 text-sm font-semibold', props.class)">
		<slot />
	</SelectLabel>
</template>
