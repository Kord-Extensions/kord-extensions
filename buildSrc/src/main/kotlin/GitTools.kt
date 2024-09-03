import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.runCommand(command: String): String {
	val output = ByteArrayOutputStream()

	exec {
		commandLine(command.split(" "))

		standardOutput = output
	}

	val result = output.toString().trim()

	println(command)
	println(result.prependIndent("-> "))

	return result
}

fun Project.runCommand(command: String, cwd: Any): String {
	val output = ByteArrayOutputStream()

	exec {
		workingDir(cwd)
		commandLine(command.split(" "))

		standardOutput = output
		errorOutput = output
	}

	val result = output.toString().trim()

	println("$cwd -> $command")
	println(result.prependIndent("-> "))

	return output.toString().trim()
}

fun Project.getCurrentGitBranch(): String {  // https://gist.github.com/lordcodes/15b2a4aecbeff7c3238a70bfd20f0931
	var gitBranch = "Unknown branch"

	try {
		gitBranch = runCommand("git rev-parse --abbrev-ref HEAD").trim()
	} catch (t: Throwable) {
		println(t)
	}

	return gitBranch
}


fun Project.getCurrentGitHash(): String {  // https://gist.github.com/lordcodes/15b2a4aecbeff7c3238a70bfd20f0931
	var gitHash = "unknown"

	try {
		gitHash = runCommand("git rev-parse --short HEAD").trim()
	} catch (t: Throwable) {
		println(t)
	}

	return gitHash
}
