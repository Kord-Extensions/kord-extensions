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

	interface SkeletonProps {
		class?: HTMLAttributes["class"]
	}

	const props = defineProps<SkeletonProps>()
</script>

<template>
	<div :class="cn('animate-pulse rounded-md bg-muted', props.class)" />
</template>
