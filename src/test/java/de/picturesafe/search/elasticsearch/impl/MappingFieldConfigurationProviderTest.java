/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MappingFieldConfigurationProviderTest {

    @Mock
    private ElasticsearchAdmin elasticsearchAdmin;

    @Test
    public void testCacheTimeout() throws Exception {
        final MappingFieldConfigurationProvider provider = new MappingFieldConfigurationProvider(elasticsearchAdmin, 100);
        final String indexAlias = "test";
        provider.getFieldConfigurations(indexAlias);
        provider.getFieldConfigurations(indexAlias);
        Thread.sleep(110);
        provider.getFieldConfigurations(indexAlias);
        verify(elasticsearchAdmin, times(2)).resolveIndexNames(eq(indexAlias));
    }
}
