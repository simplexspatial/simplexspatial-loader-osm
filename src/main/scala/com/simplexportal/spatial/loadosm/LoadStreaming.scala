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

import java.io.{File, FileInputStream, InputStream}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.EntityIterator.fromPbf
import com.acervera.osm4scala.model.{NodeEntity, OSMEntity, OSMTypes, WayEntity}
import com.simplexportal.spatial.index.grid.entrypoints.grpc._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class LoadStreaming {

  val startTime = System.currentTimeMillis()

  var ways = 0
  var nodes = 0
  var others = 0
  var blocksSent = 0

  // Boot akka
  implicit val sys = ActorSystem("LoadOSMStreaming")
  implicit val mat = ActorMaterializer()
  implicit val ec = sys.dispatcher

  // Take details how to connect to the service from the config.
  val clientSettings = GrpcClientSettings.fromConfig(GRPCEntryPoint.name)

  // Create a client-side stub for the service
  val client: GRPCEntryPoint = GRPCEntryPointClient(clientSettings)

  private def analiseFile(osmFile: File): (Long, Long, Long) =
    EntityIterator.fromPbf(new FileInputStream(osmFile)).foldLeft((0L, 0L, 0L)) {
      case ((nodes, ways, others), _: NodeEntity) => (nodes + 1, ways, others)
      case ((nodes, ways, others), _: WayEntity)  => (nodes, ways + 1, others)
      case ((nodes, ways, others), _)             => (nodes, ways, others + 1)
    }

  def loadBatches(osmFile: File, blockSize: Int, count: Boolean): Unit = {
    println(
      s"Loading batches data from [${osmFile.getAbsolutePath}]"
    )

    val (totalNodes, totalWays, totalOthers) = if (count) analiseFile(osmFile) else (-1L, -1L, -1L)
    println(
      s"Metrics before start to load data: Total nodes [${totalNodes}] | Total ways [${totalWays}] | Total others [${totalOthers}]"
    )

    run(Source.fromIterator(() => fromPbf(new FileInputStream(osmFile))), blockSize, totalNodes, totalWays, totalOthers)

  }

  def run(
      source: Source[OSMEntity, NotUsed],
      blockSize: Int,
      totalNodes: Long = -1,
      totalWays: Long = -1,
      totalOthers: Long = -1
  ): Unit = {

    println("Starting the streaming >>>>> ")

    val reply = client.streamBatchCommands(createBatchSource(source, blockSize))
      .runForeach {
        case ACK(ACK.ACKValue.Done(value), _) =>
          println(
            s"Sent ${nodes}/${totalNodes} nodes,  ${ways}/${totalWays} ways and ${others}/${totalOthers} others in ${(System
              .currentTimeMillis() - startTime) / 1000} seconds.  ${blocksSent} blocks sent. Response: ${value}"
          )
        case ACK(ACK.ACKValue.NotDone(value), _) =>
          throw new Exception(s"Error found ${value}")
        case response =>
          throw new Exception(s"Unkown response ${response}")
      }

    reply.onComplete {
      case Success(msg) =>
        println(s"got last reply for streaming requests as $msg")
        printTotals
        sys.terminate()
      case Failure(e) =>
        println(s"Error streamingRequest: $e")
        sys.terminate()
    }
  }

  def printTotals(): Unit = {
    println("Asking for metrics .....")
    val metrics = Await.result(client.getMetrics(GetMetricsCmd()), 1.hour)
    println(
      s"Added ${metrics.nodes}/${nodes} nodes and ${metrics.ways}/${ways} ways in ${(System
        .currentTimeMillis() - startTime) / 1000} seconds. ${blocksSent} blocks sent."
    )
  }

  def createBatchSource(
      source: Source[OSMEntity, NotUsed],
      blockSize: Int
  ): Source[ExecuteBatchCmd, NotUsed] = {

    source
      .filter(osmEntity => osmEntity.osmModel != OSMTypes.Relation)
      .map {
        case nodeEntity: NodeEntity =>
          nodes += 1
          ExecuteCmd().withNode(
            AddNodeCmd(
              nodeEntity.id,
              nodeEntity.longitude,
              nodeEntity.latitude,
              nodeEntity.tags
            )
          )
        case wayEntity: WayEntity =>
          ways += 1
          ExecuteCmd().withWay(
            AddWayCmd(wayEntity.id, wayEntity.nodes, wayEntity.tags)
          )
        case _ =>
          others += 1
          null
      }
      .grouped(blockSize)
      .map(cmds => {
        blocksSent += 1
        ExecuteBatchCmd().withCommands(cmds)
      })
  }

}
