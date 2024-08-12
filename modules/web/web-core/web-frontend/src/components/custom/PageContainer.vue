<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
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
