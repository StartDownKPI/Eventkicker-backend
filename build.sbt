organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.2",
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.slick"  %%  "slick"         % "3.1.0",
    "com.typesafe.slick"  %%  "slick-hikaricp"% "3.1.0",
    "com.github.tminglei" %%  "slick-pg"      % "0.10.1",
    "com.github.tototoshi"%%  "slick-joda-mapper"% "2.1.0",
    "org.mindrot"         %   "jbcrypt"       % "0.3m",
    "org.postgresql"      %   "postgresql"    % "9.3-1102-jdbc41",
    "org.slf4j"           %   "slf4j-nop"     % "1.6.4",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "joda-time"           %   "joda-time"     % "2.7",
    "org.joda"            %   "joda-convert"  % "1.7",
    "me.lessis"           %%  "courier"       % "0.1.3"
  )
}

Revolver.settings
