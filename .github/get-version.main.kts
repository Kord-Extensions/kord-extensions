import java.io.File
import java.util.Properties

val properties = Properties()

properties.load(
    File("gradle.properties").inputStream()
)

print(properties["projectVersion"])
