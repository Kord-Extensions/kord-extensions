<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
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
