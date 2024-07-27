<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import {
		MenubarRoot,
		type MenubarRootEmits,
		type MenubarRootProps,
		useForwardPropsEmits,
	} from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = defineProps<MenubarRootProps & { class?: HTMLAttributes["class"] }>()
	const emits = defineEmits<MenubarRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props

		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<MenubarRoot
		:class="
      cn(
        'flex h-10 items-center gap-x-1 rounded-md border bg-background p-1',
        props.class,
      )
    "
		v-bind="forwarded"
	>
		<slot />
	</MenubarRoot>
</template>
