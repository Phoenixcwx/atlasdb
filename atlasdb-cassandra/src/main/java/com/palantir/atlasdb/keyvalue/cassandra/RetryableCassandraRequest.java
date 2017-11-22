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

package com.palantir.atlasdb.keyvalue.cassandra;

import java.net.InetSocketAddress;

import com.palantir.common.base.FunctionCheckedException;

public class RetryableCassandraRequest<V, K extends Exception> {
    private final InetSocketAddress preferredHost;
    private final FunctionCheckedException<CassandraClient, V, K> fn;

    private int numberOfAttempts = 0;

    public RetryableCassandraRequest(InetSocketAddress preferredHost,
            FunctionCheckedException<CassandraClient, V, K> fn) {
        this.preferredHost = preferredHost;
        this.fn = fn;
    }

    public InetSocketAddress getPreferredHost() {
        return preferredHost;
    }

    public FunctionCheckedException<CassandraClient, V, K> getFunction() {
        return fn;
    }

    public void incrementNumberOfAttempts() {
        numberOfAttempts++;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }
}
