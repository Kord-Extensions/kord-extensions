/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
