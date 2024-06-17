<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { Primitive, type PrimitiveProps } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<PrimitiveProps & { class?: HTMLAttributes["class"] }>(), {
		as: "a",
	})
</script>

<template>
	<Primitive
		:as="as"
		:as-child="asChild"
		:class="cn('transition-colors hover:text-foreground', props.class)"
	>
		<slot />
	</Primitive>
</template>
