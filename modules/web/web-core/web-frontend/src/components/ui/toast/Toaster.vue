<!--
	This Source Code Form is subject to the terms of the Mozilla Public
	License, v. 2.0. If a copy of the MPL was not distributed with this
	file, You can obtain one at https://mozilla.org/MPL/2.0/.
-->

<script lang="ts" setup>
	import { isVNode } from "vue"
	import { useToast } from "./use-toast"
	import { Toast, ToastClose, ToastDescription, ToastProvider, ToastTitle, ToastViewport } from "."

	const { toasts } = useToast()
</script>

<template>
	<ToastProvider>
		<Toast v-for="toast in toasts" :key="toast.id" v-bind="toast">
			<div class="grid gap-1">
				<ToastTitle v-if="toast.title">
					{{ toast.title }}
				</ToastTitle>
				<template v-if="toast.description">
					<ToastDescription v-if="isVNode(toast.description)">
						<component :is="toast.description" />
					</ToastDescription>
					<ToastDescription v-else>
						{{ toast.description }}
					</ToastDescription>
				</template>
				<ToastClose />
			</div>
			<component :is="toast.action" />
		</Toast>
		<ToastViewport />
	</ToastProvider>
</template>
