<script lang="ts" setup>
	import { type HTMLAttributes, computed } from "vue"
	import { PinInputRoot, type PinInputRootEmits, type PinInputRootProps, useForwardPropsEmits } from "radix-vue"
	import { cn } from "@/lib/utils"

	const props = withDefaults(defineProps<PinInputRootProps & { class?: HTMLAttributes["class"] }>(), {
		modelValue: () => [],
	})
	const emits = defineEmits<PinInputRootEmits>()

	const delegatedProps = computed(() => {
		const { class: _, ...delegated } = props
		return delegated
	})

	const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
	<PinInputRoot :class="cn('flex gap-2 items-center', props.class)" v-bind="forwarded">
		<slot />
	</PinInputRoot>
</template>
