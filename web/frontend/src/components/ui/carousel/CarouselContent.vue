<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { useCarousel } from "./useCarousel"
	import type { WithClassAsProps } from "./interface"
	import { cn } from "@/lib/utils"

	defineOptions({
		inheritAttrs: false,
	})

	const props = defineProps<WithClassAsProps>()

	const { carouselRef, orientation } = useCarousel()
</script>

<template>
	<div ref="carouselRef" class="overflow-hidden">
		<div
			:class="
        cn(
          'flex',
          orientation === 'horizontal' ? '-ml-4' : '-mt-4 flex-col',
          props.class,
        )"
			v-bind="$attrs"
		>
			<slot />
		</div>
	</div>
</template>
