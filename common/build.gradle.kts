dependencies {
  implementation(project(":pacman-lib"))
}

scoverage {
  minimumRate.set(BigDecimal("0.3"))
}
