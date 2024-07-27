<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
	<ol
		:class="cn('flex flex-wrap items-center gap-1.5 break-words text-sm text-muted-foreground sm:gap-2.5', props.class)"
	>
		<slot />
	</ol>
</template>
