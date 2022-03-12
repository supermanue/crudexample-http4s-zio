name := "zio-experiment"
version := "0.1"
scalaVersion := "2.13.3"

val Http4sVersion          = "0.21.22"
val CirceVersion           = "0.13.0"
val DoobieVersion          = "0.12.1"
val ZIOVersion             = "1.0.6"
val PureConfigVersion      = "0.17.1"
val ZIOInteropVersion      = "2.4.0.0"
val Log4JVersion           = "1.7.36"
val OrganizeImportsVersion = "0.6.0"
val ScalaTestVersion = "3.2.11"

libraryDependencies ++= Seq(
  // ZIO
  "dev.zio"          %% "zio"              % ZIOVersion,
  "com.github.wi101" %% "embroidery"       % "0.1.1",
  "dev.zio"          %% "zio-interop-cats" % ZIOInteropVersion,
  "dev.zio"          %% "zio-test"         % ZIOVersion % "test",
  "dev.zio"          %% "zio-test-sbt"     % ZIOVersion % "test",
  // Http4s
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe"        % Http4sVersion,
  "org.http4s" %% "http4s-dsl"          % Http4sVersion,
  "org.http4s" %% "http4s-client"       % Http4sVersion,
  // Circe
  "io.circe" %% "circe-generic"        % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceVersion,
  // Doobie
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-h2"   % DoobieVersion,
  //pure config
  "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
  // log4j
  "org.slf4j" % "slf4j-log4j12" % Log4JVersion,
  //test
  "org.scalactic" %% "scalactic" % ScalaTestVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"

)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint"
)
scalafmtOnCompile := true

// scalafix; run with `scalafixEnable` followed by `scalafixAll`
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % OrganizeImportsVersion

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
