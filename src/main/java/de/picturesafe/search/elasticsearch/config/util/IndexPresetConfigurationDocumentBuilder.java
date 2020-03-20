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

package de.picturesafe.search.elasticsearch.config.util;

import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;

import java.util.Map;

public class IndexPresetConfigurationDocumentBuilder {

    private IndexPresetConfigurationDocumentBuilder() {
    }

    public static Map<String, Object> build(IndexPresetConfiguration conf) {
        return DocumentBuilder.withoutId()
                .put("class", conf.getClass().getName())
                .put("indexAlias", conf.getIndexAlias())
                .put("numberOfShards", conf.getNumberOfShards())
                .put("numberOfReplicas", conf.getNumberOfReplicas())
                .put("maxResultWindow", conf.getMaxResultWindow())
                .put("fieldsLimit", conf.getFieldsLimit())
                .put("useCompression", conf.isUseCompression())
                .put("charMappings", conf.getCharMappings())
                .put("customTokenizers", conf.getCustomTokenizers())
                .put("customAnalyzers", conf.getCustomAnalyzers())
                .build();
    }
}
