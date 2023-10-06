import com.kotlindiscord.kord.extensions.plugins.PluginManager
import com.kotlindiscord.kord.extensions.plugins.test.core.TestPlugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PluginJarTests {
	private val pluginManager = PluginManager<TestPlugin>(
		baseTypeReference = "TestPlugin",
		pluginDirectory = "tmp/plugins"
	)

	@Test
	fun `Standard plugin load`() {
		pluginManager.loadAllPlugins()

		Assertions.assertEquals(
			"test-one",
			pluginManager.loadPlugin("test-one")?.get()?.manifest?.id
		)

		Assertions.assertEquals(
			"test-two",
			pluginManager.loadPlugin("test-two")?.get()?.manifest?.id
		)
	}
}
