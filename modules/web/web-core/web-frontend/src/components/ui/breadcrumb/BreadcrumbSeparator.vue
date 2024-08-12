<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { HTMLAttributes } from "vue"
	import { ChevronRight } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<{
		class?: HTMLAttributes["class"]
	}>()
</script>

<template>
	<li
		:class="cn('[&>svg]:size-3.5', props.class)"
		aria-hidden="true"
		role="presentation"
	>
		<slot>
			<ChevronRight />
		</slot>
	</li>
</template>
