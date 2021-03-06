/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.paxos;

import java.io.IOException;

public final class PaxosStateLogs {

    private PaxosStateLogs() {
        // Private constructor. Disallow instantiation.
    }

    public static byte[] getGreatestValidLogEntry(PaxosStateLog<?> log) {
        long greatestValid = log.getGreatestLogEntry();

        long least = log.getLeastLogEntry();
        if (greatestValid == PaxosAcceptor.NO_LOG_ENTRY) {
            return null;
        }

        while (greatestValid >= least && greatestValid != PaxosAcceptor.NO_LOG_ENTRY) {
            try {
                byte[] bytes = log.readRound(greatestValid);
                if (bytes != null) {
                    return bytes;
                } else {
                    greatestValid--;
                }
            } catch (IOException e) {
                greatestValid--;
            }
        }

        return null;
    }

}
