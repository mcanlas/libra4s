// format: off
lazy val root =
  Project("libra4s", file("."))
    .aggregate(core, web)

lazy val web =
  module("web")
    .dependsOn(core)
    .withHttpServer
    .withHtmlTemplating
    .withJson
    .withLogging
    .withTesting
    .enablePlugins(JavaAppPackaging, DockerPlugin)
    .settings(
      dockerExposedPorts := Seq(8080),

      // https://github.com/typelevel/cats-effect/issues/4306
      // dockerBaseImage := "eclipse-temurin:21"
      bashScriptExtraDefines += """addJava "-Dcats.effect.warnOnNonMainThreadDetected=false""""
    )

lazy val core =
  module("core")
    .withEffectMonad
    .withLogging
    .withTesting
    .withGitHubPackagesCredentials
    .withResolver("rufio")
