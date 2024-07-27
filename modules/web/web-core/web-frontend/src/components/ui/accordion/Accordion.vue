<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import {
		AccordionRoot,
		type AccordionRootEmits,
		type AccordionRootProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<AccordionRootProps>()
	const emits = defineEmits<AccordionRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<AccordionRoot v-bind="forwarded">
		<slot />
	</AccordionRoot>
</template>
