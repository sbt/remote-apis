Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / version := {
  val old = (ThisBuild / version).value
  if ((ThisBuild / isSnapshot).value) "2.3.0-SNAPSHOT"
  else old
}
val prefix = "com.eed3si9n.remoteapis.shaded"
ThisBuild / organization := prefix
ThisBuild / publishMavenStyle := true
ThisBuild / description  := "An API for caching and execution of actions on a remote system."
ThisBuild / homepage     := Some(url("https://github.com/eed3si9n/remote-apis"))
ThisBuild / licenses     := Seq("Apache-v2" -> url("https://github.com/eed3si9n/remote-apis/blob/main/LICENSE"))
ThisBuild / assemblyMergeStrategy := {
  case PathList("com", "eed3si9n", "remoteapis", xs @ _*) => MergeStrategy.first
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

ThisBuild / assemblyShadeRules := Seq(
  ShadeRule.rename("android.**" -> s"$prefix.android.@1").inAll,
  ShadeRule.rename("com.**" -> s"$prefix.com.@1").inAll,
  ShadeRule.rename("io.**" -> s"$prefix.io.@1").inAll,
  ShadeRule.rename("org.**" -> s"$prefix.org.@1").inAll,
)

lazy val root = (project in file("."))
  .aggregate(shaded)
  .enablePlugins(ProtobufPlugin)
  .settings(
    name := "remoteapis-java",
    protobufGrpcEnabled := true,
    ProtobufConfig / sourceDirectories += (ProtobufConfig / protobufExternalIncludePath).value,
    ProtobufConfig  / protobufExcludeFilters ++= {
      val dirs = (ProtobufConfig  / sourceDirectories).value
      dirs.flatMap(d =>
        Seq(
          Glob(d.toPath()) / "google" / "api" / "auth.proto",
          Glob(d.toPath()) / "google" / "api" / "billing.proto",
          Glob(d.toPath()) / "google" / "api" / "log.proto",
          Glob(d.toPath()) / "google" / "api" / "context.proto",
          Glob(d.toPath()) / "google" / "api" / "control.proto",
          Glob(d.toPath()) / "google" / "type" / "color.proto",
          Glob(d.toPath()) / "google" / "type" / "date.proto",
          Glob(d.toPath()) / "google" / "type" / "dayofweek.proto",
          Glob(d.toPath()) / "google" / "type" / "money.proto",
          Glob(d.toPath()) / "google" / "type" / "timeofday.proto",
        )
      )
    },
    autoScalaLibrary := false,
    crossPaths := false,
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % protobufGrpcVersion.value,
      "io.grpc" % "grpc-protobuf" % protobufGrpcVersion.value,
      "io.grpc" % "grpc-stub" % protobufGrpcVersion.value,
      "com.google.api.grpc" % "googleapis-common-protos" % "0.0.3" % ProtobufConfig
    ),
    publish / skip := true,
  )

lazy val shaded = (project in file("shaded"))
  .settings(
    name := "shaded-remoteapis-java",
    autoScalaLibrary := false,
    crossPaths := false,
    Compile / packageBin := (LocalProject("root") / assembly).value
  )

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/eed3si9n/remote-apis"),
    "scm:git@github.com:eed3si9n/remote-apis.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "eed3si9n",
    name  = "Eugene Yokota",
    email = "@eed3si9n",
    url   = url("https://eed3si9n.com/")
  )
)
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  val v = (ThisBuild / version).value
  if (v.endsWith("-SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
