import java.io.File
import java.util.Properties

val properties = Properties()
val file = File("gradle.properties")

properties.load(file.inputStream())

properties["org.gradle.parallel"] = "false"

properties.store(file.outputStream(), null)

println("Disabled parallel builds. New file contents:")
println(file.readText().prependIndent("    "))
