# README.md

## Prerequisites

- Maven `sudo apt-get install maven`; this will simplify dependency management (the SCCF uses Maven heavily, so it is worthwhile to learn). The `pom.xml` file is set up to automatically use the Soot dependencies in the Maven repository; they may also be pulled locally via `mvn install` in the directory where the `pom.xml` exists, this will copy the jar files into your ~/.m2 directory.

## Generate the sample Hello World forward branched PDG.

