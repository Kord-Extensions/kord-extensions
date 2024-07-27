/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import type {
	EmblaCarouselType as CarouselApi,
	EmblaOptionsType as CarouselOptions,
	EmblaPluginType as CarouselPlugin,
} from "embla-carousel"
import type { HTMLAttributes, Ref } from "vue"

export interface CarouselProps {
	opts?: CarouselOptions | Ref<CarouselOptions>
	plugins?: CarouselPlugin[] | Ref<CarouselPlugin[]>
	orientation?: "horizontal" | "vertical"
}

export interface CarouselEmits {
	(e: "init-api", payload: CarouselApi): void
}

export interface WithClassAsProps {
	class?: HTMLAttributes["class"]
}
