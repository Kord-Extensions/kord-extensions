<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { ChevronRight } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
	<li
		:class="cn('[&>svg]:size-3.5', props.class)"
		aria-hidden="true"
		role="presentation"
	>
		<slot>
			<ChevronRight />
		</slot>
	</li>
</template>
