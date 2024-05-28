<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
	<caption :class="cn('mt-4 text-sm text-muted-foreground', props.class)">
		<slot />
	</caption>
</template>
