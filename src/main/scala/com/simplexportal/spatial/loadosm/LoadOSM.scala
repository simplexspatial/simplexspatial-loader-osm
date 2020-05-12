/*
 * Copyright 2020 SimplexPortal Ltd
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

import java.io.{File, FileInputStream}

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model.{NodeEntity, OSMEntity, WayEntity}
import com.simplexportal.spatial.index.grid.entrypoints.grpc.ExecuteCmd.Command
import com.simplexportal.spatial.index.grid.entrypoints.grpc._

object LoadOSM {

  object Metrics {
    def apply(commands: Seq[ExecuteCmd]): Metrics = commands.foldLeft(Metrics()) { (metrics, cmd) =>
      cmd.command match {
        case Command.Node(_) => metrics.copy(nodes = metrics.nodes + 1)
        case Command.Way(_)  => metrics.copy(ways = metrics.ways + 1)
        case _               => metrics.copy(others = metrics.others + 1)
      }
    }
  }

  case class Metrics(nodes: Long = 0, ways: Long = 0, others: Long = 0) {
    def reduce(b: Metrics): Metrics = copy(nodes = nodes + b.nodes, ways = ways + b.ways, others = others + b.others)
    override def toString(): String = s"nodes [$nodes] / ways [$ways] / others [$others]"
  }

  def sourceFromOSM(osm: File): Source[OSMEntity, NotUsed] =
    Source
      .fromIterator { () =>
        EntityIterator.fromPbf(new FileInputStream(osm))
      }
      .named("osm file reader")

  /**
    * Group data in blocks and create batches.
    * @param entitiesPerBlock Max. number of entities per block.
    */
  def groupEntities(entitiesPerBlock: Int): Flow[OSMEntity, ExecuteBatchCmd, NotUsed] = {
    Flow[OSMEntity]
      .collect {
        case WayEntity(id, nodes, tags) =>
          ExecuteCmd().withWay(AddWayCmd(id, nodes, tags))
        case NodeEntity(id, latitude, longitude, tags) =>
          ExecuteCmd().withNode(AddNodeCmd(id, longitude, latitude, tags))
      }
      .grouped(entitiesPerBlock)
      .map( ExecuteBatchCmd().withCommands )

//      .batch(entitiesPerBlock, (executeCmd) => ExecuteBatchCmd().withCommands(Seq(executeCmd))) {
//        (batchCmd, executeCmd) =>
//          batchCmd.addCommands(executeCmd)
//      }
  }

  private val progress =  Flow[ExecuteBatchCmd].fold(Metrics()) { (metrics, cmds) =>
    val batchMetrics = Metrics(cmds.commands)
    val newMetrics = metrics.reduce(batchMetrics)
    println(s"Ingested ${metrics} => Sent ${batchMetrics}}")
    newMetrics
  }.to(Sink.foreach( m => println(s"Ingested ${m}}")))

  /**
    * Start the ingestion.
    */
  def runFlow(osm: File, maxEntitiesPerBlock: Int, client: GRPCEntryPoint): Source[ACK, NotUsed] = {
    val entities =
      sourceFromOSM(osm)
        .via(groupEntities(maxEntitiesPerBlock))
        .alsoTo(progress)

    client.streamBatchCommands(entities)
  }

}
