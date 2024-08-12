<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		NavigationMenuRoot,
		type NavigationMenuRootEmits,
		type NavigationMenuRootProps,
		useForwardPropsEmits,
	} from "radix-vue"
	import NavigationMenuViewport from "./NavigationMenuViewport.vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<NavigationMenuRootProps & { class?: HTMLAttributes["class"] }>()

	const emits = defineEmits<NavigationMenuRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<NavigationMenuRoot
		:class="cn('relative z-10 flex max-w-max flex-1 items-center justify-center', props.class)"
		v-bind="forwarded"
	>
		<slot />
		<NavigationMenuViewport />
	</NavigationMenuRoot>
</template>
