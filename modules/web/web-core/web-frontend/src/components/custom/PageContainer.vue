<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script setup lang="ts">

</script>

<template>
	<div class="flex flex-row items-stretch h-full">
<!--		<div-->
<!--			class="border-r hidden md:w-[17em] md:max-w-[17em] md:flex md:flex-col sticky h-full overflow-hidden sidebar"-->
<!--			style="top: calc(4em + 1px);">-->
<!--			<slot name="sidebar" />-->
<!--		</div>-->

		<div class="flex-grow w-full pt-2 !px-4">
			<slot />
		</div>
	</div>
</template>

<style scoped>

</style>
