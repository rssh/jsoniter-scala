val `jsoniter-scala-examples` = project.in(file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    scalaVersion := "2.13.5",
    scalacOptions ++= Seq("-Xmacro-settings:print-codecs"),
    libraryDependencies ++= Seq(
      "org.scalatest"                     %% "scalatest"                % "3.0.8" % Test,
      "org.scalamock"                     %% "scalamock"                % "4.3.0" % Test,
      "io.circe"                          %% "circe-generic"            % "0.13.0",
      "io.circe"                          %% "circe-parser"             % "0.13.0",
      "io.circe"                          %% "circe-optics"             % "0.13.0",
      "io.circe"                          %% "circe-yaml"               % "0.13.1",
      // Use the %%% operator instead of %% for Scala.js
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.7.2",
      // Use the "provided" scope instead when the "compile-internal" scope is not supported
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.7.2" % "provided",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2"
    ).map(_ exclude ("javax.ws.rs", "javax.ws.rs-api"))
  )
