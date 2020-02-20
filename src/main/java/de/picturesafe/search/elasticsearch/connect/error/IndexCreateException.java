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

package de.picturesafe.search.elasticsearch.connect.error;

import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Exception in case of failed index creation
 */
public final class IndexCreateException extends ElasticsearchException {

    private final IndexPresetConfiguration indexPresetConfiguration;
    private final MappingConfiguration mappingConfiguration;

    public IndexCreateException(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) {
        super("Failed to create index:\n" + confToString(indexPresetConfiguration, mappingConfiguration));
        this.indexPresetConfiguration = indexPresetConfiguration;
        this.mappingConfiguration = mappingConfiguration;
    }

    public IndexCreateException(String message, IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) {
        super(message + "\n" + confToString(indexPresetConfiguration, mappingConfiguration));
        this.indexPresetConfiguration = indexPresetConfiguration;
        this.mappingConfiguration = mappingConfiguration;
    }

    public IndexCreateException(String message, Throwable cause, IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) {
        super(message + "\n" + confToString(indexPresetConfiguration, mappingConfiguration), cause);
        this.indexPresetConfiguration = indexPresetConfiguration;
        this.mappingConfiguration = mappingConfiguration;
    }

    public IndexCreateException(Throwable cause, IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) {
        super("Failed to create index:\n" + confToString(indexPresetConfiguration, mappingConfiguration), cause);
        this.indexPresetConfiguration = indexPresetConfiguration;
        this.mappingConfiguration = mappingConfiguration;
    }

    public IndexPresetConfiguration getIndexPresetConfiguration() {
        return indexPresetConfiguration;
    }

    public MappingConfiguration getMappingConfiguration() {
        return mappingConfiguration;
    }

    private static String confToString(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) {
        return "preset configuration = " + indexPresetConfiguration + ", mapping configuration = " + mappingConfiguration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("indexPresetConfiguration", indexPresetConfiguration) //--
                .append("mappingConfiguration", mappingConfiguration) //--
                .toString();
    }
}
