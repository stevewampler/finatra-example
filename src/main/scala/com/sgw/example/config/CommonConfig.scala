package com.sgw.example.config

import java.io.File

import com.twitter.util.Duration
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.readers.ValueReader
import net.ceedubs.ficus.Ficus._

object CommonConfig {

  implicit val durationReader = new ValueReader[Duration] {
    override def read(config: Config, path: String) = Duration.parse(config.as[String](path))
  }

  implicit val optionDurationReader = new ValueReader[Option[Duration]] {
    override def read(config: Config, path: String) = if (config.hasPath(path)) Some(Duration.parse(config.getString(path))) else None
  }

  def apply(file: File): CommonConfig = {
    CommonConfig(ConfigFactory.parseFile(file).getConfig("changeme"))
  }

  def apply(config: Config): CommonConfig = {
    CommonConfig(
//      deployEnv = config.as[String]("deployEnv"),
      /* TODO */
    )
  }
}

case class CommonConfig(/* TODO */)