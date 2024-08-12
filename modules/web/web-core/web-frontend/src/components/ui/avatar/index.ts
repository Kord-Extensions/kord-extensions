/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

import { type VariantProps, cva } from "class-variance-authority"

export { default as Avatar } from "./Avatar.vue"
export { default as AvatarImage } from "./AvatarImage.vue"
export { default as AvatarFallback } from "./AvatarFallback.vue"

export const avatarVariant = cva(
	"inline-flex items-center justify-center font-normal text-foreground select-none shrink-0 bg-secondary overflow-hidden",
	{
		variants: {
			size: {
				sm: "h-10 w-10 text-xs",
				base: "h-16 w-16 text-2xl",
				lg: "h-32 w-32 text-5xl",
			},
			shape: {
				circle: "rounded-full",
				square: "rounded-md",
			},
		},
	},
)

export type AvatarVariants = VariantProps<typeof avatarVariant>
