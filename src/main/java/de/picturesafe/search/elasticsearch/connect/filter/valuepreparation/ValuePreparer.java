/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.valuepreparation;

public interface ValuePreparer {

    void prepare(ValuePrepareContext valuePrepareContext);

}
