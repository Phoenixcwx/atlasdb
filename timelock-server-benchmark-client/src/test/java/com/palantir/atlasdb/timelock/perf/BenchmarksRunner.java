/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.palantir.atlasdb.timelock.perf;

import org.junit.Test;

/**
 * Note that there is no warmup time included in any of these tests, so if the server has just been started you'll want
 * to execute many requests until the results stabilize (give the JIT compiler time to optimize).
 */
public class BenchmarksRunner extends BenchmarkRunnerBase {

    @Test
    public void timestamp() {
        runAndPrintResults(client::timestamp, 4, 1000);
    }

    @Test
    public void lockAndUnlockUncontended() {
        runAndPrintResults(client::lockAndUnlockUncontended, 4, 500);
    }

    @Test
    public void lockAndUnlockContended() {
        runAndPrintResults(() -> client.lockAndUnlockContended(8, 1000, 2));
    }

    @Test
    public void writeTransactionRows() {
        runAndPrintResults(() -> client.writeTransactionRows(1, 20, 1000, 200));
    }

    @Test
    public void writeTransactionDynamicColumns() {
        runAndPrintResults(() -> client.writeTransactionDynamicColumns(1, 20, 1000, 200));
    }


    @Test
    public void readTransactionRows() {
        runAndPrintResults(() -> client.readTransactionRows(1, 20, 10_000, 200));
    }

    @Test
    public void kvsCas() {
        runAndPrintResults(client::kvsCas, 1, 5000);
    }

    @Test
    public void kvsWrite() {
        runAndPrintResults(client::kvsWrite, 1, 1000);
    }

    @Test
    public void kvsRead() {
        runAndPrintResults(client::kvsRead, 1, 5000);
    }

    @Test
    public void contendedWriteTransaction() {
        runAndPrintResults(client::contendedWriteTransaction, 2000, 1);
    }

    @Test
    public void rowsRangeScan() {
        runAndPrintResults(() -> client.rangeScanRows(1, 20, 200, 1_000));

    }
    @Test
    public void dynamicColumnsRangeScan() {
        runAndPrintResults(() -> client.rangeScanDynamicColumns(16, 20, 1000, 10_000));
    }

}

