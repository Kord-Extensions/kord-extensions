<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
