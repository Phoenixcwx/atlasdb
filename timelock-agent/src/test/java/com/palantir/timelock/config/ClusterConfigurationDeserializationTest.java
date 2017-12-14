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

package com.palantir.timelock.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.common.collect.ImmutableSet;
import com.palantir.atlasdb.config.AtlasDbConfigs;

public class ClusterConfigurationDeserializationTest {
    private static final String LOCAL_SERVER = "https://server-2:8421";
    private static final Set<String> SERVERS = ImmutableSet.of(
            "https://server-1:8421", LOCAL_SERVER, "https://server-3:8421");

    private static final File CLUSTER_CONFIG_NO_TYPE_INFO = getClusterConfigFile("no-type-info");
    private static final File CLUSTER_CONFIG_DEFAULT_TYPE_INFO = getClusterConfigFile("default-type-info");
    private static final File CLUSTER_CONFIG_INVALID_TYPE_INFO = getClusterConfigFile("invalid-type-info");
    private static final File CLUSTER_CONFIG_MALFORMED = getClusterConfigFile("malformed");
    private static final File CLUSTER_CONFIG_KUBERNETES = getClusterConfigFile("kubernetes");
    private static final ObjectMapper OBJECT_MAPPER = AtlasDbConfigs.OBJECT_MAPPER;

    @Test
    public void canDeserializeClusterConfigurationWithoutTypeInformation() throws IOException {
        assertDefaultClusterConfigurationCorrect(deserializeClusterConfiguration(CLUSTER_CONFIG_NO_TYPE_INFO));
    }

    @Test
    public void canDeserializeClusterConfigurationWithDefaultTypeInformation() throws IOException {
        assertDefaultClusterConfigurationCorrect(deserializeClusterConfiguration(CLUSTER_CONFIG_DEFAULT_TYPE_INFO));
    }

    @Test
    public void canDeserializeClusterConfigurationWithInvalidTypeInformation() throws IOException {
        // TODO (jkong): This case should fail, or at least log an informative warning; but for now it passes.
        assertDefaultClusterConfigurationCorrect(deserializeClusterConfiguration(CLUSTER_CONFIG_INVALID_TYPE_INFO));
    }

    @Test
    public void throwsOnMalformedClusterConfiguration() throws IOException {
        assertThatThrownBy(() -> deserializeClusterConfiguration(CLUSTER_CONFIG_MALFORMED))
                .isInstanceOf(UnrecognizedPropertyException.class)
                .hasMessageContaining("my-node-set");
    }

    @Test
    public void doesNotDeserializeKubernetesClusterConfigurationAsDefaultClusterConfiguration() throws IOException {
        // This is somewhat limited by the design of KubernetesHostnames, but should at least confirm that we're
        // attempting to deserialize a k8s configuration as one, and not as a default configuration
        assertThatThrownBy(() -> deserializeClusterConfiguration(CLUSTER_CONFIG_KUBERNETES))
                .hasMessageContaining("k8s stateful set");
    }

    private void assertDefaultClusterConfigurationCorrect(ClusterConfiguration cluster) {
        assertThat(cluster)
                .isInstanceOf(DefaultClusterConfiguration.class)
                .isNotInstanceOf(KubernetesClusterConfiguration.class);
        assertThat(cluster.clusterMembers()).hasSameElementsAs(SERVERS);
        assertThat(cluster.localServer()).isEqualTo(LOCAL_SERVER);
    }

    private static ClusterConfiguration deserializeClusterConfiguration(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, ClusterConfiguration.class);
    }

    private static File getClusterConfigFile(String clusterConfigType) {
        return new File(ClusterConfigurationDeserializationTest.class.getResource(
                String.format("/cluster-configuration-%s.yml", clusterConfigType)).getPath());
    }
}
