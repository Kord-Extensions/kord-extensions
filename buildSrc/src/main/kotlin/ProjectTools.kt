import gradle.kotlin.dsl.accessors._6f0d4de64e119f738e1f647c2e25e6b5.ext
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
