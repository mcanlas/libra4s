import sbt.Keys.*
import sbt.*

object DependenciesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    implicit class DependencyOps(p: Project) {
      val http4sVersion =
        "0.23.33"

      val circeVersion =
        "0.14.15"

      def withEffectMonad: Project =
        p
          .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0")

      def withLogging: Project =
        p.settings(
          libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.17"
        )

      def withHtmlTemplating =
        p
          .settings(
            libraryDependencies ++= Seq(
              "com.lihaoyi" %% "scalatags" % "0.13.1"
            )
          )

      def withHttpClient =
        p
          .settings(
            libraryDependencies ++= Seq(
              "org.http4s" %% "http4s-ember-client" % http4sVersion,
              "org.http4s" %% "http4s-circe"        % http4sVersion
            )
          )

      def withHttpServer =
        p
          .settings(
            libraryDependencies ++= Seq(
              "org.http4s" %% "http4s-ember-server" % http4sVersion,
              "org.http4s" %% "http4s-dsl"          % http4sVersion,
              "org.http4s" %% "http4s-scalatags"    % "0.25.2",
              "org.http4s" %% "http4s-circe"        % http4sVersion
            )
          )

      def withJson: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "io.circe" %% "circe-generic" % circeVersion,
            "io.circe" %% "circe-parser"  % circeVersion,
          )
        )

      def withConfigInjection: Project =
        p.settings(
          libraryDependencies += "is.cir" %% "ciris" % "3.6.0"
        )

      def withTesting: Project = {
        val weaverVersion =
          "0.12.0"

        p.settings(
          libraryDependencies ++= Seq(
            "org.typelevel" %% "weaver-cats"       % weaverVersion % Test,
            "org.typelevel" %% "weaver-scalacheck" % weaverVersion % Test
          )
        )
      }
    }
  }
}
