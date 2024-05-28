<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { ChevronRight } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
	<li
		:class="cn('[&>svg]:size-3.5', props.class)"
		aria-hidden="true"
		role="presentation"
	>
		<slot>
			<ChevronRight />
		</slot>
	</li>
</template>
