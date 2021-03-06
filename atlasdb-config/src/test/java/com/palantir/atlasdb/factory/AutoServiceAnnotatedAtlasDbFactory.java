/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
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
package com.palantir.atlasdb.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jmock.Mockery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.palantir.atlasdb.config.LeaderConfig;
import com.palantir.atlasdb.keyvalue.api.KeyValueService;
import com.palantir.atlasdb.keyvalue.api.TableReference;
import com.palantir.atlasdb.qos.QosClient;
import com.palantir.atlasdb.spi.AtlasDbFactory;
import com.palantir.atlasdb.spi.KeyValueServiceConfig;
import com.palantir.timestamp.TimestampService;

@AutoService(AtlasDbFactory.class)
public class AutoServiceAnnotatedAtlasDbFactory implements AtlasDbFactory {
    public static final String TYPE = "not-a-real-db";

    private static final Mockery context = new Mockery();
    private static final KeyValueService keyValueService = context.mock(KeyValueService.class);
    private static List<TimestampService> nextTimestampServices = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(AutoServiceAnnotatedAtlasDbFactory.class);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public KeyValueService createRawKeyValueService(
            KeyValueServiceConfig config,
            Optional<LeaderConfig> leaderConfig,
            Optional<String> unused,
            boolean initializeAsync,
            QosClient unusedQosClient) {
        if (initializeAsync) {
            log.warn("Asynchronous initialization not implemented, will initialize synchronousy.");
        }

        return keyValueService;
    }

    @Override
    public TimestampService createTimestampService(KeyValueService rawKvs, Optional<TableReference> timestampTable,
            boolean initializeAsync) {
        return nextTimestampServices.remove(0);
    }

    public static void nextTimestampServiceToReturn(TimestampService... timestampServices) {
        nextTimestampServices = Lists.newArrayList(timestampServices);
    }
}
