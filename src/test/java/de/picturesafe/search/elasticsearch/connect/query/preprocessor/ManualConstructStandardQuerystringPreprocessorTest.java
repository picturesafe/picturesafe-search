package de.picturesafe.search.elasticsearch.connect.query.preprocessor;

import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import org.junit.Before;

public class ManualConstructStandardQuerystringPreprocessorTest extends AbstractStandardQuerystringPreprocessorTest {

    @Before
    public void setup() {
        preprocessor = new StandardQuerystringPreprocessor(new QueryConfiguration());
    }
}
