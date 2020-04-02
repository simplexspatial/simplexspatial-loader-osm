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

import com.acervera.osm4scala.model.{NodeEntity, OSMEntity, WayEntity}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import akka.stream.scaladsl.Source
import com.acervera.osm4scala.EntityIterator.fromPbf
import com.fasterxml.jackson.core.`type`.TypeReference

import scala.collection.immutable.Seq

class LoadStreamingSpec extends AnyWordSpecLike with Matchers {

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  "Load" should {
    "Load one way example" when {
      val entities: scala.collection.immutable.Seq[OSMEntity] =
        mapper.readValue(new File("src/test/resources/6165679/nodes.json"), new TypeReference[Seq[NodeEntity]]() {}) ++
          mapper.readValue(new File("src/test/resources/6165679/ways.json"), new TypeReference[Seq[WayEntity]]() {})

      new LoadStreaming().run(Source(entities), 10)
    }
    "load full Monaco streaming in batch mode" when {
      "it uses block of 300" in {
//        new LoadStreaming().run(
//          Source.fromIterator(() =>
//            fromPbf(
//              new FileInputStream(
//                "/home/angelcc/projects/simplexspatial-loader-osm/src/test/resources/monaco-latest.osm.pbf"
//              )
//            )
//          ),
//          300
//        )
        new LoadStreaming().loadBatches(
          new File("/home/angelcc/projects/simplexspatial-loader-osm/src/test/resources/monaco-latest.osm.pbf"),
          300,
          true
        )
      }
    }
  }
}
