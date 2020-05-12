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

import com.acervera.osm4scala.EntityIterator
import com.acervera.osm4scala.model.{NodeEntity, WayEntity}

object Analyzer {

  /**
   * Return number of nodes, ways and other entities.
   *
   * @param osmFile pbf format file
   * @return (nodes, ways, others)
   */
  def analyze(osmFile: File): (Long, Long, Long) =
    EntityIterator.fromPbf(new FileInputStream(osmFile)).foldLeft((0L, 0L, 0L)) {
      case ((nodes, ways, others), _: NodeEntity) => (nodes + 1, ways, others)
      case ((nodes, ways, others), _: WayEntity)  => (nodes, ways + 1, others)
      case ((nodes, ways, others), _)             => (nodes, ways, others + 1)
    }
}
