/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.plugins.weblogs.stages

import java.text.SimpleDateFormat

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.weblogs.HttpdLogItem
import org.codefeedr.stages.InputStage

/**
  * Source that streams Apache log files
  *
  * @param absolutePath Absolute file path to the log
  */
class HttpdLogInput(absolutePath: String, stageId: Option[String] = None)
    extends InputStage[HttpdLogItem](stageId)
    with Serializable {

  override def main(context: Context): DataStream[HttpdLogItem] = {
    context.env
      .readTextFile(absolutePath)
      .setParallelism(1)
      .flatMap(_.split('\n'))
      .flatMap(new LogMapper())
      .assignAscendingTimestamps(_.date.getTime / 1000)
  }

}

private class LogMapper extends FlatMapFunction[String, HttpdLogItem] {

  lazy val dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z")
  lazy val pattern =
    """^(\S+) \S+ \S+ \[([\w:\/]+\s[+\-]\d{4})\] "(\S+)\s+(\S+)\s+(\S+)?\s*" (\d{3}) (\S+) ("[^"]*") ("[^"]*") ("[^"]*")""".r

  def flatMap(line: String, out: Collector[HttpdLogItem]): Unit = line match {
    case pattern(ipAddress,
                 dateString,
                 method,
                 path,
                 version,
                 status,
                 amountOfBytes,
                 referer,
                 userAgent,
                 _*) => {
      val date = dateFormat.parse(dateString)
      val amountOfBytesInt =
        if (amountOfBytes != "-") amountOfBytes.toInt else -1

      out.collect(
        HttpdLogItem(ipAddress,
                     date,
                     method,
                     path,
                     version,
                     status.toInt,
                     amountOfBytesInt,
                     referer,
                     userAgent))
    }
    case _ =>
  }

}
