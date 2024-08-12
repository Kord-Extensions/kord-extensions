<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { TagsInputItemDelete, type TagsInputItemDeleteProps, useForwardProps } from "radix-vue"
	import { X } from "lucide-vue-next"
	import { cn } from "@/lib/utils"

	const props = defineProps<TagsInputItemDeleteProps & { class?: HTMLAttributes["class"] }>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
	<TagsInputItemDelete :class="cn('flex rounded bg-transparent mr-1', props.class)" v-bind="forwardedProps">
		<slot>
			<X class="w-4 h-4" />
		</slot>
	</TagsInputItemDelete>
</template>
