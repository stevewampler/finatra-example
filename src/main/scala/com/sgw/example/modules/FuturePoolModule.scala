package com.sgw.example.modules

import java.util.concurrent.{ExecutorService, Executors, ForkJoinPool, ThreadPoolExecutor}

import com.google.inject.{Provides, Singleton}
import com.sgw.example._
import com.twitter.inject.TwitterModule
import com.twitter.util.FuturePool

import scala.concurrent.ExecutionContext

object FuturePoolModule extends TwitterModule {
  // These are casted to ThreadPoolExecutor so that we can monitor their sizes
  val ioExecutor : ThreadPoolExecutor = Executors.newCachedThreadPool().asInstanceOf[ThreadPoolExecutor]
  val computeExecutor : ForkJoinPool =  Executors.newWorkStealingPool().asInstanceOf[ForkJoinPool]

  @Singleton
  @Provides
  @ComputeFuturePool
  def computeFuturePool: FuturePool = FuturePool(computeExecutor) // intended for compute concurrency

  @Singleton
  @Provides
  @IOFuturePool
  def ioFuturePool: FuturePool = FuturePool(ioExecutor)// intended for IO concurrency

  @Singleton
  @Provides
  @IOExecutionContext
  def ioExecutionContext : ExecutionContext = ExecutionContext.fromExecutorService(ioExecutor)

  @Singleton
  @Provides
  @ComputeExecutionContext
  def computeExecutionContext : ExecutionContext = ExecutionContext.fromExecutorService(computeExecutor)


  @Singleton
  @Provides
  @ComputeExecutorService
  def computeExecutorService : ExecutorService = computeExecutor

  @Singleton
  @Provides
  @IOExecutorService
  def ioExecutorService : ExecutorService = ioExecutor
}
