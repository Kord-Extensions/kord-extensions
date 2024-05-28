<script lang="ts" setup>
	import { CollapsibleContent, type CollapsibleContentProps } from "radix-vue"

	const props = defineProps<CollapsibleContentProps>()
</script>

<template>
	<CollapsibleContent class="overflow-hidden transition-all data-[state=closed]:animate-collapsible-up data-[state=open]:animate-collapsible-down"
											v-bind="props">
		<slot />
	</CollapsibleContent>
</template>
