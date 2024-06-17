<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { useCarousel } from "./useCarousel"
	import type { WithClassAsProps } from "./interface"
	import { cn } from "@/lib/utils"

	const props = defineProps<WithClassAsProps>()

	const { orientation } = useCarousel()
</script>

<template>
	<div
		:class="cn(
      'min-w-0 shrink-0 grow-0 basis-full',
      orientation === 'horizontal' ? 'pl-4' : 'pt-4',
      props.class,
    )"
		aria-roledescription="slide"
		role="group"
	>
		<slot />
	</div>
</template>
