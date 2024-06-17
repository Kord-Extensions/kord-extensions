<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { useForwardPropsEmits } from "radix-vue"
	import type { DialogRootEmits, DialogRootProps } from "radix-vue"
	import Command from "./Command.vue"
	import { Dialog, DialogContent } from "@/components/ui/dialog"

	const props = defineProps<DialogRootProps>()
	const emits = defineEmits<DialogRootEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<Dialog v-bind="forwarded">
		<DialogContent class="overflow-hidden p-0 shadow-lg">
			<Command
				class="[&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-muted-foreground [&_[cmdk-group]:not([hidden])_~[cmdk-group]]:pt-0 [&_[cmdk-group]]:px-2 [&_[cmdk-input-wrapper]_svg]:h-5 [&_[cmdk-input-wrapper]_svg]:w-5 [&_[cmdk-input]]:h-12 [&_[cmdk-item]]:px-2 [&_[cmdk-item]]:py-3 [&_[cmdk-item]_svg]:h-5 [&_[cmdk-item]_svg]:w-5">
				<slot />
			</Command>
		</DialogContent>
	</Dialog>
</template>
