/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

export { default as ChartTooltip } from "./ChartTooltip.vue"
export { default as ChartSingleTooltip } from "./ChartSingleTooltip.vue"
export { default as ChartLegend } from "./ChartLegend.vue"
export { default as ChartCrosshair } from "./ChartCrosshair.vue"

export function defaultColors(count: number = 3) {
	const quotient = Math.floor(count / 2)
	const remainder = count % 2

	const primaryCount = quotient + remainder
	const secondaryCount = quotient
	return [
		...Array.from(Array(primaryCount).keys()).map(i => `hsl(var(--vis-primary-color) / ${1 - (1 / primaryCount) * i})`),
		...Array.from(Array(secondaryCount).keys()).map(i => `hsl(var(--vis-secondary-color) / ${1 - (1 / secondaryCount) * i})`),
	]
}

export * from "./interface"
