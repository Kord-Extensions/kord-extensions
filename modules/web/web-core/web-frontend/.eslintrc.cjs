/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

module.exports = {
	root: true,

	parserOptions: {
		parser: require.resolve("@typescript-eslint/parser"),
		extraFileExtensions: [".vue"],
	},

	env: {
		browser: true,
		es2021: true,
		node: true,
		"vue/setup-compiler-macros": true,
	},

	extends: [
		"plugin:@typescript-eslint/recommended",

		"plugin:vue/vue3-essential", // Priority A: Essential (Error Prevention)
		"plugin:vue/vue3-strongly-recommended", // Priority B: Strongly Recommended (Improving Readability)
		"plugin:vue/vue3-recommended", // Priority C: Recommended (Minimizing Arbitrary Choices and Cognitive Overhead)

		"prettier",
	],

	plugins: [
		"@typescript-eslint",

		"vue",
	],

	globals: {
		ga: "readonly", // Google Analytics
		cordova: "readonly",
		__statics: "readonly",
		process: "readonly",
		Capacitor: "readonly",
		chrome: "readonly",
	},

	rules: {
		"prefer-promise-reject-errors": "off",

		quotes: ["warn", "double", { avoidEscape: true }],

		"@typescript-eslint/explicit-function-return-type": "off",
		"@typescript-eslint/no-var-requires": "off",

		"no-unused-vars": "off",

		// allow debugger during development only
		"no-debugger": process.env.NODE_ENV === "production" ? "error" : "off",
	},
}
