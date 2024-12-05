tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
    delete("${rootProject.projectDir}/.kotlin")
}
