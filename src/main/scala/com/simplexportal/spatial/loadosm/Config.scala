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

import java.io.File

object Config {
  def apply(args: Array[String]): Option[Config] =
    new scopt.OptionParser[Config]("ExtractEntities") {
      opt[File]('i', "osmFile")
        .required()
        .valueName("<osm file>")
        .action((x, c) => c.copy(osmFile = x))
        .text("Osm file")
      opt[Int]('s', "blockSize")
        .required()
        .valueName("<block size>")
        .action((x, c) => c.copy(blockSize = x))
        .text("Commands per request")
      opt[Boolean]('c', "count")
        .valueName("<count before start load>")
        .action((x, c) => c.copy(count = x))
        .optional()
        .text("Will count all entities before to start the load")

    }.parse(args, Config(new File("/tmp"), 0, true))
}

case class Config(
    osmFile: File,
    blockSize: Int,
    count: Boolean
)
