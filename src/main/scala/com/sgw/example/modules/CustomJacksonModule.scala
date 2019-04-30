package com.sgw.example.modules

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.finatra.json.utils.CamelCasePropertyNamingStrategy
import play.libs.Json

object CustomJacksonModule extends FinatraJacksonModule {
  override val additionalJacksonModules = Seq(DefaultScalaModule)

  override val serializationInclusion = Include.NON_NULL

  override val propertyNamingStrategy = CamelCasePropertyNamingStrategy

  override def additionalMapperConfiguration(mapper: ObjectMapper): Unit = {
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

    // set play json to use our object mapper configuration
    Json.setObjectMapper(mapper)
  }
}
