/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.TimeZone;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractTimeZoneRelatedTest extends AbstractElasticIntegrationTest {

    @Autowired
    @Qualifier("elasticsearchTimeZone")
    protected String timeZone;

    private TimeZone tzBeforeTest;

    @Before
    public final void setTimeZone() {
        tzBeforeTest = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

    @After
    public final void resetTimeZone() {
        TimeZone.setDefault(tzBeforeTest);
    }
}