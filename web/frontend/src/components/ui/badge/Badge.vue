<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { type BadgeVariants, badgeVariants } from "."
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		variant?: BadgeVariants["variant"]
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
	<div :class="cn(badgeVariants({ variant }), props.class)">
		<slot />
	</div>
</template>
