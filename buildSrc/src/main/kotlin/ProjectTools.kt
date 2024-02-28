import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.getByType

class MetadataBuilder {
	lateinit var name: String
	lateinit var description: String
}

fun Project.metadata(body: (MetadataBuilder).() -> Unit) {
	val builder = MetadataBuilder()

	body(builder)

	extensions.getByType<ExtraPropertiesExtension>().apply {
		set("pubName", builder.name)
		set("pubDesc", builder.description)
	}
}
