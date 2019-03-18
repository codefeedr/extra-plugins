name := "extra-plugins"
lazy val scala212 = "2.12.8"
lazy val supportedScalaVersions = List(scala212)



ThisBuild / organization := "org.codefeedr"
ThisBuild / organizationName := "CodeFeedr"
ThisBuild / organizationHomepage := Some(url("http://codefeedr.org"))
ThisBuild / version := "0.1.0"
ThisBuild / organization := "org.codefeedr"
ThisBuild / scalaVersion := scala212


parallelExecution in Test := false

// PROJECTS

val projectPrefix = "codefeedr-"
val pluginPrefix = projectPrefix + "plugin-"

lazy val root = (project in file("."))
  .settings(settings)
  .aggregate(
    pluginRss,
    pluginTravis,
    pluginWeblogs,
    pluginTwitter)

lazy val pluginRss = (project in file("codefeedr-plugins/codefeedr-rss"))
  .settings(
    name := pluginPrefix + "rss",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.httpj
    )
  )

lazy val pluginTravis = (project in file("codefeedr-plugins/codefeedr-travis"))
  .settings(
    name := pluginPrefix + "travis",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.httpj
    )
  )

lazy val pluginWeblogs = (project in file("codefeedr-plugins/codefeedr-weblogs"))
  .settings(
    name := pluginPrefix + "weblogs",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
    )
  )

lazy val pluginTwitter = (project in file("codefeedr-plugins/codefeedr-twitter"))
  .settings(
    name := pluginPrefix + "twitter",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.twitter
    ),
    dependencyOverrides ++= Seq( //override json4s dependencies
      "org.json4s" %% "json4s-scalap" % "3.5.3",
      "org.json4s" %% "json4s-jackson" % "3.5.3",
      "org.json4s" %% "json4s-ext" % "3.5.3"
    )
  )

lazy val dependencies =
  new {
    val flinkVersion       = "1.7.0"
    val json4sVersion      = "3.6.4"
    val log4jVersion       = "2.11.0"
    val log4jScalaVersion  = "11.0"
    val codefeedrVersion   = "0.1.0"

    val loggingApi         = "org.apache.logging.log4j"   % "log4j-api"                      % log4jVersion
    val loggingCore        = "org.apache.logging.log4j"   % "log4j-core"                     % log4jVersion      % Runtime
    val loggingScala       = "org.apache.logging.log4j"  %% "log4j-api-scala"                % log4jScalaVersion

    val flink              = "org.apache.flink"          %% "flink-scala"                    % flinkVersion      % Provided
    val flinkStreaming     = "org.apache.flink"          %% "flink-streaming-scala"          % flinkVersion      % Provided
    val flinkKafka         = "org.apache.flink"          %% "flink-connector-kafka-0.11"     % flinkVersion
    val flinkRuntimeWeb    = "org.apache.flink"          %% "flink-runtime-web"              % flinkVersion      % Provided
    val flinkElasticSearch = "org.apache.flink"          %% "flink-connector-elasticsearch6" % flinkVersion
    val flinkRabbitMQ      = "org.apache.flink"          %% "flink-connector-rabbitmq"       % flinkVersion

    val codefeedrCore      = "org.codefeedr"             %% "codefeedr-core"                 % codefeedrVersion
    val codefeedrGitHub    = "org.codefeedr.plugins"     %% "codefeedr-github"               % codefeedrVersion

    val redis              = "net.debasishg"             %% "redisclient"                    % "3.6"
    val kafkaClient        = "org.apache.kafka"           % "kafka-clients"                  % "1.0.0"
    val zookeeper          = "org.apache.zookeeper"       % "zookeeper"                      % "3.4.9"

    val json4s             = "org.json4s"                %% "json4s-scalap"                  % json4sVersion
    val jackson            = "org.json4s"                %% "json4s-jackson"                 % json4sVersion
    val json4sExt          = "org.json4s"                %% "json4s-ext"                     % json4sVersion

    val mongo              = "org.mongodb.scala"         %% "mongo-scala-driver"             % "2.3.0"

    val httpj              = "org.scalaj"                %% "scalaj-http"                    % "2.4.0"

    val kryoChill          = "com.twitter"               %% "chill"                          % "0.9.1"

    val scalactic          = "org.scalactic"             %% "scalactic"                      % "3.0.1"           % Test
    val scalatest          = "org.scalatest"             %% "scalatest"                      % "3.0.5"           % Test
    val scalamock          = "org.scalamock"             %% "scalamock"                      % "4.1.0"           % Test
    val mockito            = "org.mockito"                % "mockito-all"                    % "1.10.19"         % Test
    val embeddedRedis      = "com.github.sebruck"        %% "scalatest-embedded-redis"       % "0.3.0"           % Test
    val embeddedKafka      = "net.manub"                 %% "scalatest-embedded-kafka"       % "2.0.0"           % Test
    val embeddedMongo      = "com.github.simplyscala"    %% "scalatest-embedmongo"           % "0.2.4"           % Test
    //val embeddedRabbitMQ   = "io.arivera.oss"            %% "embedded-rabbitmq"              % "1.3.0"           % Test

    val avro               = "org.apache.avro"            % "avro"                           % "1.8.2"
    val twitter            = "com.danielasfregola"        %% "twitter4s"                     % "5.5"
  }

lazy val commonDependencies = Seq(
  dependencies.flink,
  dependencies.flinkStreaming,

  dependencies.loggingApi,
  dependencies.loggingCore,
  dependencies.loggingScala,

  dependencies.scalactic,
  dependencies.scalatest,
  dependencies.scalamock,
  dependencies.mockito,
  dependencies.codefeedrCore
)


lazy val settings = commonSettings

lazy val commonSettings = Seq(
  test in assembly := {},
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "confluent"                               at "http://packages.confluent.io/maven/",
    "Apache Development Snapshot Repository"  at "https://repository.apache.org/content/repositories/snapshots/",
    "Artima Maven Repository"                 at "http://repo.artima.com/releases",
    Resolver.mavenLocal
  ),
  publishMavenStyle in ThisBuild := true,
  publishTo in ThisBuild := Some(
    if (version.value.trim.endsWith("SNAPSHOT"))
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  ThisBuild / pomIncludeRepository := { _ => false },
  crossScalaVersions := supportedScalaVersions
)

lazy val compilerOptions = Seq(
  //  "-unchecked",
  //  "-feature",
  //  "-language:existentials",
  //  "-language:higherKinds",
  //  "-language:implicitConversions",
  //  "-language:postfixOps",
  //  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*)  => MergeStrategy.discard
    case "log4j.properties"             => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

// MAKING FLINK WORK

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)

