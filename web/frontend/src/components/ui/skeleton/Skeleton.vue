<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { cn } from "@/lib/utils"

	interface SkeletonProps {
		class?: HTMLAttributes["class"]
	}

	const props = defineProps<SkeletonProps>()
</script>

<template>
	<div :class="cn('animate-pulse rounded-md bg-muted', props.class)" />
</template>
