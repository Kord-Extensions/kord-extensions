/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

export { default as Carousel } from "./Carousel.vue"
export { default as CarouselContent } from "./CarouselContent.vue"
export { default as CarouselItem } from "./CarouselItem.vue"
export { default as CarouselPrevious } from "./CarouselPrevious.vue"
export { default as CarouselNext } from "./CarouselNext.vue"
export { useCarousel } from "./useCarousel"

export type {
	EmblaCarouselType as CarouselApi,
} from "embla-carousel"
