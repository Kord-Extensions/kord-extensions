<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { TooltipProvider, type TooltipProviderProps } from "radix-vue"

	const props = defineProps<TooltipProviderProps>()
</script>

<template>
	<TooltipProvider v-bind="props">
		<slot />
	</TooltipProvider>
</template>
