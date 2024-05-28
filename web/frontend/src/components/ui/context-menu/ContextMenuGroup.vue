<script lang="ts" setup>
	import { ContextMenuGroup, type ContextMenuGroupProps } from "radix-vue"

	const props = defineProps<ContextMenuGroupProps>()
</script>

<template>
	<ContextMenuGroup v-bind="props">
		<slot />
	</ContextMenuGroup>
</template>
