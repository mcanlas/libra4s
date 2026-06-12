import sbt.Keys.*
import sbt.*

object DependenciesPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    implicit class DependencyOps(p: Project) {
      def withEffectMonad: Project =
        p
          .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % Versions.catsEffect)

      def withLogging: Project =
        p.settings(
          libraryDependencies += "org.slf4j" % "slf4j-simple" % Versions.slf4jSimple
        )

      def withRufio: Project =
        p.settings(
          libraryDependencies += "com.htmlism" %% "rufio-cats" % Versions.rufio
        )

      def withHtmlTemplating =
        p
          .settings(
            libraryDependencies ++= Seq(
              "com.lihaoyi" %% "scalatags" % Versions.scalatags
            )
          )

      def withHttpClient =
        p
          .settings(
            libraryDependencies ++= Seq(
              "org.http4s" %% "http4s-ember-client" % Versions.http4s,
              "org.http4s" %% "http4s-circe"        % Versions.http4s
            )
          )

      def withHttpServer =
        p
          .settings(
            libraryDependencies ++= Seq(
              "org.http4s" %% "http4s-ember-server" % Versions.http4s,
              "org.http4s" %% "http4s-dsl"          % Versions.http4s,
              "org.http4s" %% "http4s-scalatags"    % Versions.http4sScalatags,
              "org.http4s" %% "http4s-circe"        % Versions.http4s
            )
          )

      def withJson: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "io.circe" %% "circe-generic" % Versions.circe,
            "io.circe" %% "circe-parser"  % Versions.circe,
          )
        )

      def withConfigInjection: Project =
        p.settings(
          libraryDependencies += "is.cir" %% "ciris" % "3.6.0"
        )

      def withTesting: Project =
        p.settings(
          libraryDependencies ++= Seq(
            "org.typelevel" %% "weaver-cats"       % Versions.weaver % Test,
            "org.typelevel" %% "weaver-scalacheck" % Versions.weaver % Test
          )
        )
    }
  }
}
