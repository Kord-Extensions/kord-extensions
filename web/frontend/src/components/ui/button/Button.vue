<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { Primitive, type PrimitiveProps } from "radix-vue"
	import { type ButtonVariants, buttonVariants } from "."
	import { cn } from "@/lib/utils"

	interface Props extends PrimitiveProps {
		variant?: ButtonVariants["variant"]
		size?: ButtonVariants["size"]
		class?: HTMLAttributes["class"]
	}

	const props = withDefaults(defineProps<Props>(), {
		as: "button",
	})
</script>

<template>
	<Primitive
		:as="as"
		:as-child="asChild"
		:class="cn(buttonVariants({ variant, size }), props.class)"
	>
		<slot />
	</Primitive>
</template>
