<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { ArrowRight } from "lucide-vue-next"
	import { useCarousel } from "./useCarousel"
	import type { WithClassAsProps } from "./interface"
	import { cn } from "@/lib/utils"
	import { Button } from "@/components/ui/button"

	const props = defineProps<WithClassAsProps>()

	const { orientation, canScrollNext, scrollNext } = useCarousel()
</script>

<template>
	<Button
		:class="cn(
      'touch-manipulation absolute h-8 w-8 rounded-full p-0',
      orientation === 'horizontal'
        ? '-right-12 top-1/2 -translate-y-1/2'
        : '-bottom-12 left-1/2 -translate-x-1/2 rotate-90',
      props.class,
    )"
		:disabled="!canScrollNext"
		variant="outline"
		@click="scrollNext"
	>
		<slot>
			<ArrowRight class="h-4 w-4 text-current" />
		</slot>
	</Button>
</template>
