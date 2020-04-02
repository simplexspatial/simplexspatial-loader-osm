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

package com.simplexportal.spatial.loadosm.utilities

import java.io.{File, FileInputStream, FileWriter, PrintWriter}

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model.{NodeEntity, WayEntity}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.tmos.arm.ArmMethods._

import scala.collection.Set

object ExtractEntities {

  case class Node(id: Long, lat: Double, lon: Double, attributes: Map[String, String])
  case class Way(id: Long, nodes: Seq[Long], attributes: Map[String, String])

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def main(args: Array[String]): Unit =
    Config(args) match {
      case Some(config) =>
        if(config.nodeIds.isEmpty && config.wayIds.isEmpty) {
          extractAll(config.osmFile, config.outFileNodes, config.outFileWays)
        } else {
          extract(config.osmFile, config.outFileNodes, config.outFileWays,  config.nodeIds.toSet, config.wayIds.toSet)
        }
      case None =>
    }

  def writeNode(writer: PrintWriter, e: NodeEntity) = writer.println(mapper.writeValueAsString(Node(e.id, e.latitude, e.longitude, e.tags)))
  def writeWay(writer: PrintWriter, e: WayEntity) = writer.println(mapper.writeValueAsString( Way(e.id, e.nodes, e.tags)))

  def extractAll(pbfFile: File, jsonNodesFile: File, jsonWaysFile: File): Unit = {
    for (
      nodesWriter <- manage(new PrintWriter(jsonNodesFile));
      waysWriter <- manage(new PrintWriter(jsonWaysFile));
      pbfIS <- manage( new FileInputStream(pbfFile))
    ) {
      EntityIterator.fromPbf(pbfIS).foreach{
        case e: NodeEntity => writeNode(nodesWriter, e)
        case e: WayEntity => writeWay(waysWriter, e)
        case _ =>
      }
    }
  }

  def extract(pbfFile: File, jsonNodesFile: File, jsonWaysFile: File, nodeIds: Set[Long], wayIds: Set[Long]): Unit =
    for (
      nodesWriter <- manage(new PrintWriter(jsonNodesFile));
      waysWriter <- manage(new PrintWriter(jsonWaysFile));
      pbfIS <- manage( new FileInputStream(pbfFile))
    ) {
      EntityIterator.fromPbf(pbfIS).foreach{
        case e: NodeEntity => if(nodeIds.contains(e.id)) writeNode(nodesWriter, e)
        case e: WayEntity => if(wayIds.contains(e.id)) writeWay(waysWriter, e)
        case _ =>
      }
    }

  object Config {
    def apply(args: Array[String]): Option[Config] =
      new scopt.OptionParser[Config]("ExtractEntities") {
        opt[File]('i', "pbfFile")
          .required()
          .valueName("<osm file>")
          .action((x, c) => c.copy(osmFile = x))
          .text("osm file")
        opt[File]('n', "outNodes")
          .required()
          .valueName("<out file for nodes>")
          .action((x, c) => c.copy(outFileNodes = x))
          .text("output file in JSON format, with the list of nodes extracted")
        opt[File]('w', "outWays")
          .required()
          .valueName("<out file for ways>")
          .action((x, c) => c.copy(outFileWays = x))
          .text("output file in JSON format, with the list of ways extracted")

        opt[Seq[Long]]("nodeIds").valueName("<list of nodes Ids>").optional().action((x, c) => c.copy(nodeIds = x.toSet))
        opt[Seq[Long]]("wayIds").valueName("<list of ways Ids>").optional().action((x, c) => c.copy(wayIds = x.toSet))
      }.parse(args, Config(new File("/tmp"), new File("/tmp"), new File("/tmp")))
  }

  case class Config(
      osmFile: File,
      outFileNodes: File,
      outFileWays: File,
      nodeIds: Set[Long] = Set.empty,
      wayIds: Set[Long] = Set.empty
  )
}
