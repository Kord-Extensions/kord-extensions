<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { Primitive, type PrimitiveProps, useForwardProps } from "radix-vue"
	import { Dot } from "lucide-vue-next"

	const props = defineProps<PrimitiveProps>()
	const forwardedProps = useForwardProps(props)
</script>

<template>
	<Primitive v-bind="forwardedProps">
		<slot>
			<Dot />
		</slot>
	</primitive>
</template>
