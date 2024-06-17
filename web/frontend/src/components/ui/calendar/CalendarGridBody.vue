<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { CalendarGridBody, type CalendarGridBodyProps } from "radix-vue"

	const props = defineProps<CalendarGridBodyProps>()
</script>

<template>
	<CalendarGridBody v-bind="props">
		<slot />
	</CalendarGridBody>
</template>
