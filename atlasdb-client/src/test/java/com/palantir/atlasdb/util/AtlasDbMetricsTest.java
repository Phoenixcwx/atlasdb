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
package com.palantir.atlasdb.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.palantir.tritium.metrics.registry.DefaultTaggedMetricRegistry;
import com.palantir.tritium.metrics.registry.TaggedMetricRegistry;

public class AtlasDbMetricsTest {

    private static final String CUSTOM_METRIC_NAME = "foo";
    private static final String PING_REQUEST = "ping";
    private static final String PING_RESPONSE = "pong";

    private static final MetricRegistry metricRegistry = mock(MetricRegistry.class);
    private static final TaggedMetricRegistry taggedMetricRegistry = mock(TaggedMetricRegistry.class);

    @Rule
    public MetricsRule metricsRule = new MetricsRule();

    @Before
    public void before() throws Exception {
        AtlasDbMetrics.metrics.set(null);
        AtlasDbMetrics.taggedMetrics.set(null);
    }

    @Test
    public void metricsIsDefaultWhenNotSet() {
        assertThat(AtlasDbMetrics.getMetricRegistry(),
                is(equalTo(SharedMetricRegistries.getOrCreate(AtlasDbMetrics.DEFAULT_REGISTRY_NAME))));
    }

    @Test
    public void taggedMetricsIsDefaultWhenNotSet() {
        assertThat(AtlasDbMetrics.getTaggedMetricRegistry(),
                is(equalTo(DefaultTaggedMetricRegistry.getDefault())));
    }

    @Test
    public void metricsAreNotDefaultWhenSet() {
        AtlasDbMetrics.setMetricRegistries(metricRegistry, taggedMetricRegistry);
        assertThat(AtlasDbMetrics.getMetricRegistry(), is(equalTo(metricRegistry)));
        assertThat(AtlasDbMetrics.getTaggedMetricRegistry(), is(equalTo(taggedMetricRegistry)));
    }

    @Test(expected = NullPointerException.class)
    public void nullMetricsCannotBeSet() {
        AtlasDbMetrics.setMetricRegistries(null, taggedMetricRegistry);
    }

    @Test(expected = NullPointerException.class)
    public void nullTaggedMetricsCannotBeSet() {
        AtlasDbMetrics.setMetricRegistries(metricRegistry, null);
    }

    @Test
    public void instrumentWithDefaultName() throws Exception {
        MetricRegistry metrics = setMetricRegistry();
        TestService service = AtlasDbMetrics.instrument(TestService.class, () -> PING_RESPONSE);

        assertMetricCountIncrementsAfterPing(metrics, service, MetricRegistry.name(TestService.class, PING_REQUEST));
    }

    @Test
    public void instrumentWithCustomName() throws Exception {
        MetricRegistry metrics = setMetricRegistry();
        TestService service = AtlasDbMetrics.instrument(TestService.class, () -> PING_RESPONSE, CUSTOM_METRIC_NAME);

        assertMetricCountIncrementsAfterPing(metrics, service, MetricRegistry.name(CUSTOM_METRIC_NAME, PING_REQUEST));
    }

    private MetricRegistry setMetricRegistry() {
        MetricRegistry metrics = SharedMetricRegistries.getOrCreate("AtlasDbTest");
        TaggedMetricRegistry taggedMetrics = DefaultTaggedMetricRegistry.getDefault();
        AtlasDbMetrics.setMetricRegistries(metrics, taggedMetrics);
        return metrics;
    }

    private void assertMetricCountIncrementsAfterPing(
            MetricRegistry metrics,
            TestService service,
            String methodTimerName) {
        assertThat(metrics.timer(methodTimerName).getCount(), is(equalTo(0L)));

        assertThat(service.ping(), is(equalTo(PING_RESPONSE)));

        assertThat(metrics.timer(methodTimerName).getCount(), is(equalTo(1L)));
    }

    public interface TestService {
        String ping();
    }
}
