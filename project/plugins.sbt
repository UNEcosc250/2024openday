// The Typesafe repository
resolvers += "Typesafe repository" at "https://dl.bintray.com/typesafe/maven-releases/"

// Use the Play sbt plugin for Play projects
//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.15.0")
addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "1.1.0")
addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.0")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.2.0")
addSbtPlugin("io.spray"                  % "sbt-revolver"              % "0.9.1")
addSbtPlugin("com.eed3si9n"              % "sbt-assembly"              % "0.15.0")

addSbtPlugin("com.typesafe.sbt"          % "sbt-native-packager"       % "1.8.1")

addSbtPlugin("com.typesafe.sbt"          % "sbt-gzip"                  % "1.0.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-digest"                % "1.1.4")

