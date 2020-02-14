/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
