<script lang="ts" setup>
	import { DialogClose, type DialogCloseProps } from "radix-vue"

	const props = defineProps<DialogCloseProps>()
</script>

<template>
	<DialogClose v-bind="props">
		<slot />
	</DialogClose>
</template>
