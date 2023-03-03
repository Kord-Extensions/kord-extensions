import gradle.kotlin.dsl.accessors._0a1b0feab67dedd28ab92e8d9de7aa93.ext
import org.gradle.api.Project

class MetadataBuilder {
    public lateinit var name: String
    public lateinit var description: String
}

fun Project.metadata(body: (MetadataBuilder).() -> Unit) {
    val builder = MetadataBuilder()

    body(builder)

    ext.set("pubName", builder.name)
    ext.set("pubDesc", builder.description)
}
