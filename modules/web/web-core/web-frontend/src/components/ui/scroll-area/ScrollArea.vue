<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		ScrollAreaCorner,
		ScrollAreaRoot,
		type ScrollAreaRootProps,
		ScrollAreaViewport,
	} from "radix-vue"
	import ScrollBar from "./ScrollBar.vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<ScrollAreaRootProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<ScrollAreaRoot :class="cn('relative overflow-hidden', props.class)" v-bind="delegatedProps">
		<ScrollAreaViewport class="h-full w-full rounded-[inherit]">
			<slot />
		</ScrollAreaViewport>
		<ScrollBar />
		<ScrollAreaCorner />
	</ScrollAreaRoot>
</template>
