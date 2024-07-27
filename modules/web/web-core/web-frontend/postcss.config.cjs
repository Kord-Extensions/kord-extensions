/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
