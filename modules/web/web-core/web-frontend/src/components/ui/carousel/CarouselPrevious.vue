<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { ArrowLeft } from "lucide-vue-next"
	import { useCarousel } from "./useCarousel"
	import type { WithClassAsProps } from "./interface"
	import { cn } from "@/lib/utils"
	import { Button } from "@/components/ui/button"

	const props = defineProps<WithClassAsProps>()

	const { orientation, canScrollPrev, scrollPrev } = useCarousel()
</script>

<template>
	<Button
		:class="cn(
      'touch-manipulation absolute h-8 w-8 rounded-full p-0',
      orientation === 'horizontal'
        ? '-left-12 top-1/2 -translate-y-1/2'
        : '-top-12 left-1/2 -translate-x-1/2 rotate-90',
      props.class,
    )"
		:disabled="!canScrollPrev"
		variant="outline"
		@click="scrollPrev"
	>
		<slot>
			<ArrowLeft class="h-4 w-4 text-current" />
		</slot>
	</Button>
</template>
