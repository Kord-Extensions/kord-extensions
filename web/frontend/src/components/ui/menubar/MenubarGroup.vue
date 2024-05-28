<script lang="ts" setup>
	import { MenubarGroup, type MenubarGroupProps } from "radix-vue"

	const props = defineProps<MenubarGroupProps>()
</script>

<template>
	<MenubarGroup v-bind="props">
		<slot />
	</MenubarGroup>
</template>
