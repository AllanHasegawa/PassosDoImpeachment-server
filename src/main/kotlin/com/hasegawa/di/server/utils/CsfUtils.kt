/*******************************************************************************
 * Copyright 2016 Allan Yoshio Hasegawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hasegawa.di.server.utils

import org.apache.commons.validator.routines.InetAddressValidator
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object CsfUtils {
    fun tempBanIp(ip: String, timeInSeconds: Long): String {
        if (!isIpValid(ip)) throw RuntimeException("Ip not valid, can't execute Csf temp ban. Ip was: $ip")

        val retStr = StringBuilder()
        val process = Runtime.getRuntime().exec("csf -td $ip $timeInSeconds")
        val input = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (true) {
            line = input.readLine()
            if (line == null) {
                break
            }
            retStr.append(line)
            retStr.append(System.getProperty("line.separator"))
        }
        process.waitFor(10, TimeUnit.SECONDS)
        return retStr.toString()
    }

    private fun isIpValid(ip: String) = InetAddressValidator.getInstance().isValid(ip)
}
