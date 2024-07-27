<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { useProvideCarousel } from "./useCarousel"
	import type { CarouselEmits, CarouselProps, WithClassAsProps } from "./interface"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<CarouselProps & WithClassAsProps>(), {
		orientation: "horizontal",
	})

	const emits = defineEmits<CarouselEmits>()

	const carouselArgs = useProvideCarousel(props, emits)

	defineExpose(carouselArgs)

	function onKeyDown(event: KeyboardEvent) {
		const prevKey = props.orientation === "vertical" ? "ArrowUp" : "ArrowLeft"
		const nextKey = props.orientation === "vertical" ? "ArrowDown" : "ArrowRight"

		if (event.key === prevKey) {
			event.preventDefault()
			carouselArgs.scrollPrev()

			return
		}

		if (event.key === nextKey) {
			event.preventDefault()
			carouselArgs.scrollNext()
		}
	}
</script>

<template>
	<div
		:class="cn('relative', props.class)"
		aria-roledescription="carousel"
		role="region"
		tabindex="0"
		@keydown="onKeyDown"
	>
		<slot v-bind="carouselArgs" />
	</div>
</template>
