package com.sgw.example.modules

import java.io.File

import com.google.inject.{Provides, Singleton}
import com.sgw.example.config.CommonConfig
import com.twitter.inject.TwitterModule

object ExampleServerModule extends TwitterModule {
  val configFileFlag = flag[String]("config", "App Configuration File")

  @Singleton
  @Provides
  def configuration(): CommonConfig = configFileFlag.get.map { filePath =>
    CommonConfig(new File(filePath))
  }.getOrElse {
    CommonConfig()
  }
}
