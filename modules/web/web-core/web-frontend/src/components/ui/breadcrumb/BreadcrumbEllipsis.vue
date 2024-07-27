<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { MoreHorizontal } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
  <span
		:class="cn('flex h-9 w-9 items-center justify-center', props.class)"
		aria-hidden="true"
		role="presentation"
	>
    <slot>
      <MoreHorizontal class="h-4 w-4" />
    </slot>
    <span class="sr-only">More</span>
  </span>
</template>
