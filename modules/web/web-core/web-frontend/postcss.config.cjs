/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

/* eslint-disable */
// https://github.com/michael-ciniawsky/postcss-load-config

module.exports = {
	plugins: [
		// https://github.com/postcss/autoprefixer
		require("autoprefixer")({
			overrideBrowserslist: [
				"last 4 Chrome versions",
				"last 4 Firefox versions",
				"last 4 Edge versions",
				"last 4 Safari versions",
				"last 4 Android versions",
				"last 4 ChromeAndroid versions",
				"last 4 FirefoxAndroid versions",
				"last 4 iOS versions",
			],
		}),
	],
}
