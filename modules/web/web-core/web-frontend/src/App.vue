<!--
	Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
	with the specific provision (EUPL articles 14 & 15) that the
	applicable law is the (Republic of) Irish law and the Jurisdiction
	Dublin.
	Any redistribution must include the specific provision above.
-->

<script lang="ts" setup>
	import * as Card from "@/components/ui/card"
	import PageContainer from "@/components/custom/PageContainer.vue"
</script>

<template>
	<PageContainer>
		<Card.Card class="bg-red-300 w-1/2 text-center absolute top-1/4 left-1/4">
			<Card.CardHeader>
				<Card.CardTitle>
					Work In Progress!
				</Card.CardTitle>
			</Card.CardHeader>

			<Card.CardContent>
				<p>
					This web interface is a work in progress, and will not be ready for use for quite some time.
				</p>

				<p>
					If you'd like to follow development, please join
					<a class="text-blue-700" href="https://discord.gg/nYzQWcjAmK">the Kord Extensions Discord server</a>.
				</p>
			</Card.CardContent>
		</Card.Card>
	</PageContainer>
</template>

<style scoped>

</style>
