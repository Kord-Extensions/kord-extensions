import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun Project.runCommand(command: String): String {
	val output = ByteArrayOutputStream()

	exec {
		commandLine(command.split(" "))
		standardOutput = output
	}

	return output.toString().trim()
}

fun Project.getCurrentGitBranch(): String {  // https://gist.github.com/lordcodes/15b2a4aecbeff7c3238a70bfd20f0931
	var gitBranch = "Unknown branch"

	try {
		gitBranch = runCommand("git rev-parse --abbrev-ref HEAD")
	} catch (t: Throwable) {
		println(t)
	}

	return gitBranch
}
