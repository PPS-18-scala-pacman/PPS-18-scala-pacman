dependencies {
  implementation(project(":pacman-lib"))
}

scoverage {
  excludedPackages.set(listOf("it.unibo.scalapacman.common"))
}
