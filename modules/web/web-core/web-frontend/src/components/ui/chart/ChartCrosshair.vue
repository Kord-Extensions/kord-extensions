<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { VisCrosshair, VisTooltip } from "@unovis/vue"
	import type { BulletLegendItemInterface } from "@unovis/ts"
	import { omit } from "@unovis/ts"
	import { type Component, createApp } from "vue"
	import { ChartTooltip } from "."

	const props = withDefaults(defineProps<{
		colors: string[]
		index: string
		items: BulletLegendItemInterface[]
		customTooltip?: Component
	}>(), {
		colors: () => [],
	})

	// Use weakmap to store reference to each datapoint for Tooltip
	const wm = new WeakMap()

	function template(d: any) {
		if (wm.has(d)) {
			return wm.get(d)
		} else {
			const componentDiv = document.createElement("div")
			const omittedData = Object.entries(omit(d, [props.index])).map(([key, value]) => {
				const legendReference = props.items.find(i => i.name === key)
				return { ...legendReference, value }
			})
			const TooltipComponent = props.customTooltip ?? ChartTooltip
			createApp(TooltipComponent, { title: d[props.index].toString(), data: omittedData }).mount(componentDiv)
			wm.set(d, componentDiv.innerHTML)
			return componentDiv.innerHTML
		}
	}

	function color(d: unknown, i: number) {
		return props.colors[i] ?? "transparent"
	}
</script>

<template>
	<VisTooltip :horizontal-shift="20" :vertical-shift="20" />
	<VisCrosshair :color="color" :template="template" />
</template>
