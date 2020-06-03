package de.picturesafe.search.elasticsearch.connect.util;

import de.picturesafe.search.parameter.SearchAggregation;
import org.apache.commons.lang3.StringUtils;

public class ElasticAggregationUtils {

    private ElasticAggregationUtils() {
    }

    public static String aggregationName(SearchAggregation<?> aggregation) {
        return StringUtils.isNotBlank(aggregation.getName()) ? aggregation.getName() : StringUtils.substringBefore(aggregation.getField(), ".");
    }
}
