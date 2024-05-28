<script lang="ts" setup>
	import { SelectValue, type SelectValueProps } from "radix-vue"

	const props = defineProps<SelectValueProps>()
</script>

<template>
	<SelectValue v-bind="props">
		<slot />
	</SelectValue>
</template>
