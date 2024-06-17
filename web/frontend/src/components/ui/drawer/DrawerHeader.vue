<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HtmlHTMLAttributes } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HtmlHTMLAttributes["class"]
	}>()
</script>

<template>
	<div :class="cn('grid gap-1.5 p-4 text-center sm:text-left', props.class)">
		<slot />
	</div>
</template>
