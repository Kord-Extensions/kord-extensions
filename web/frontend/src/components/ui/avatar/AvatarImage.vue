<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { AvatarImage, type AvatarImageProps } from "radix-vue"

	const props = defineProps<AvatarImageProps>()
</script>

<template>
	<AvatarImage class="h-full w-full object-cover" v-bind="props" />
</template>
