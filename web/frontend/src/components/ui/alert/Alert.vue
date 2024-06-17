<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { type AlertVariants, alertVariants } from "."
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
		variant?: AlertVariants["variant"]
	}>()
</script>

<template>
	<div :class="cn(alertVariants({ variant }), props.class)" role="alert">
		<slot />
	</div>
</template>
