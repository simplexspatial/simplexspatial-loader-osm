/*
 * Copyright 2019 SimplexPortal Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.simplexportal.spatial.loadosm

import java.io.File

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.simplexportal.spatial.index.grid.entrypoints.grpc._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class LoadStreaming {

  val startTime = System.currentTimeMillis()

  // Boot akka
  implicit val sys = ActorSystem("LoadOSMStreaming")
  implicit val mat = ActorMaterializer()
  implicit val ec = sys.dispatcher

  // Take details how to connect to the service from the config.
  val clientSettings = GrpcClientSettings.fromConfig(GRPCEntryPoint.name)

  // Better, https://doc.akka.io/docs/akka-grpc/current/client/configuration.html
  // Create a client-side stub for the service
  val client: GRPCEntryPoint = GRPCEntryPointClient(clientSettings)



  def loadBatches(osmFile: File, blockSize: Int, count: Boolean): Unit = {
    println(
      s"Loading batches data from [${osmFile.getAbsolutePath}]"
    )

//    val (totalNodes, totalWays, totalOthers) = if (count) analiseFile(osmFile) else (-1L, -1L, -1L)
//    println(
//      s"Metrics before start to load data: Total nodes [${totalNodes}] | Total ways [${totalWays}] | Total others [${totalOthers}]"
//    )

    val metrics = Await.result(
      LoadOSM.runFlow(osmFile, blockSize, client).runWith(Sink.foreach(println)),
      Duration.Inf
    )

    println(metrics)
    sys.terminate()

  }

  def printTotals(nodes: Long, ways: Long, others: Long, blocksSent: Long): Unit = {
    println("Asking for metrics .....")
    val metrics = Await.result(client.getMetrics(GetMetricsCmd()), 1.hour)
    println(
      s"Added ${metrics.nodes}/${nodes} nodes and ${metrics.ways}/${ways} ways in ${(System
        .currentTimeMillis() - startTime) / 1000} seconds. ${blocksSent} blocks sent."
    )
  }


}
