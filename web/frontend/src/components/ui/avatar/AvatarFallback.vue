<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { AvatarFallback, type AvatarFallbackProps } from "radix-vue"

	const props = defineProps<AvatarFallbackProps>()
</script>

<template>
	<AvatarFallback v-bind="props">
		<slot />
	</AvatarFallback>
</template>
