<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { DropdownMenuTrigger, type DropdownMenuTriggerProps, useForwardProps } from "radix-vue"

	const props = defineProps<DropdownMenuTriggerProps>()

	const forwardedProps = useForwardProps(props)
</script>

<template>
	<DropdownMenuTrigger class="outline-none" v-bind="forwardedProps">
		<slot />
	</DropdownMenuTrigger>
</template>
