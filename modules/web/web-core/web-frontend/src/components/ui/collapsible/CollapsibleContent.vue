<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

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
