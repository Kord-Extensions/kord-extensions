<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { AvatarRoot } from "radix-vue"
	import { type AvatarVariants, avatarVariant } from "."
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<{
		class?: HTMLAttributes["class"]
		size?: AvatarVariants["size"]
		shape?: AvatarVariants["shape"]
	}>(), {
		size: "sm",
		shape: "circle",
	})
</script>

<template>
	<AvatarRoot :class="cn(avatarVariant({ size, shape }), props.class)">
		<slot />
	</AvatarRoot>
</template>
