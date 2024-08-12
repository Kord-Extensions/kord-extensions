<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import type { DrawerDescriptionProps } from "vaul-vue"
	import { DrawerDescription } from "vaul-vue"
	import { type HtmlHTMLAttributes, computed } from "vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<DrawerDescriptionProps & { class?: HtmlHTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})
</script>

<template>
	<DrawerDescription :class="cn('text-sm text-muted-foreground', props.class)" v-bind="delegatedProps">
		<slot />
	</DrawerDescription>
</template>
