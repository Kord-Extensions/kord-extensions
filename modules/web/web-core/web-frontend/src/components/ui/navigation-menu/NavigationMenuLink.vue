<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import {
		NavigationMenuLink,
		type NavigationMenuLinkEmits,
		type NavigationMenuLinkProps,
		useForwardPropsEmits,
	} from "radix-vue"

	const props = defineProps<NavigationMenuLinkProps>()
	const emits = defineEmits<NavigationMenuLinkEmits>()

	const forwarded = useForwardPropsEmits(props, emits)
</script>

<template>
	<NavigationMenuLink v-bind="forwarded">
		<slot />
	</NavigationMenuLink>
</template>
