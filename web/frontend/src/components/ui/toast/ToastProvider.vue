<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { ToastProvider, type ToastProviderProps } from "radix-vue"

	const props = defineProps<ToastProviderProps>()
</script>

<template>
	<ToastProvider v-bind="props">
		<slot />
	</ToastProvider>
</template>
