package com.sgw.example.services

import com.sgw.example.web.controllers.graphql.FooSchema.CreateFooInput
import com.twitter.util.Future

object Foo {
  def apply(input: CreateFooInput): Foo = Foo(
    input.id,
    input.name
  )
}

case class Foo(
  id: Long,
  name: String
)

case class FooService() {
  private var idToFooMap = Map[Long, Foo]()

  def ping(): Future[String] = Future.value("pong")

  def createFoo(input: CreateFooInput): Future[Foo] = Future {
    val foo = Foo(input)

    this.synchronized {
      idToFooMap = idToFooMap.updated(input.id, foo)
    }

    foo
  }
}
