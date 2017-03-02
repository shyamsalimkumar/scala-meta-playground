name := "scala-meta-playground"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalameta" %% "scalameta" % "1.6.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-beta4" cross CrossVersion.full)

scalacOptions += "-Xplugin-require:macroparadise"
