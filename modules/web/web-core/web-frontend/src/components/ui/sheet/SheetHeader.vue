<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<{ class?: HTMLAttributes["class"] }>()
</script>

<template>
	<div
		:class="
      cn('flex flex-col gap-y-2 text-center sm:text-left', props.class)
    "
	>
		<slot />
	</div>
</template>
