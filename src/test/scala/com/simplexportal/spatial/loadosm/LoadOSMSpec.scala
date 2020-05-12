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

import com.simplexportal.spatial.loadosm.MonacoFacts._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._

class LoadOSMSpec extends AnyWordSpecLike with Matchers with AkkaSpec {
  "sourceFromOSM" should {
    "read the full file" in {
      Await.result(
        LoadOSM.sourceFromOSM(new File("src/test/resources/monaco-latest.osm.pbf"))
        .runFold( 0L) { case ( counter, _) => counter + 1
        }, 500.milli
      ) shouldBe (TOTAL_ENTITIES)
    }
  }
}
