<script lang="ts" setup>
	import { AvatarFallback, type AvatarFallbackProps } from "radix-vue"

	const props = defineProps<AvatarFallbackProps>()
</script>

<template>
	<AvatarFallback v-bind="props">
		<slot />
	</AvatarFallback>
</template>
