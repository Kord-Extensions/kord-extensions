val build = task("build", Exec::class) {
    commandLine("mkdocs", "build", "-c", "-d", "build/mkdocs")
}

val serve = task("serve", Exec::class) {
    commandLine("mkdocs", "serve")
}
