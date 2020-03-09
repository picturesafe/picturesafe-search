package de.picturesafe.search.elasticsearch.connect.query.preprocessor;

import de.picturesafe.search.spring.configuration.TestConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, SpringBeanStandardQuerystringPreprocessorTest.Config.class},
        loader = AnnotationConfigContextLoader.class)
public class SpringBeanStandardQuerystringPreprocessorTest extends AbstractStandardQuerystringPreprocessorTest {

    @Autowired
    private StandardQuerystringPreprocessor standardQuerystringPreprocessor;

    @Before
    public void setup() {
        preprocessor = standardQuerystringPreprocessor;
    }

    @Configuration
    @ComponentScan(basePackages = "de.picturesafe.search.elasticsearch.connect")
    protected static class Config {
    }
}
