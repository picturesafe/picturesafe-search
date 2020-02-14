/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import de.picturesafe.search.elasticsearch.connect.AbstractTimeZoneRelatedTest;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ElasticDateUtilsTest extends AbstractTimeZoneRelatedTest {

    private static final String STANDARD_TIMESTAMP_FORMAT = "dd.MM.yyyy HH:mm:ss";

    @Test
    public void testFormatIsoUtilDate() throws Exception {
        final Date date = DateUtils.parseDate("26.04.2017 11:51:31", STANDARD_TIMESTAMP_FORMAT);
        final String isoDate = ElasticDateUtils.formatIso(date, timeZone);
        final String offset = ElasticDateUtils.getOffset(timeZone, date);
        assertEquals("2017-04-26T11:51:31" + offset, isoDate);
    }

    @Test
    public void testFormatIsoSqlDate() throws Exception {
        final Date date = DateUtils.parseDate("26.04.2017 11:51:31", STANDARD_TIMESTAMP_FORMAT);
        final java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        final String isoDate = ElasticDateUtils.formatIso(sqlDate, timeZone);
        final String offset = ElasticDateUtils.getOffset(timeZone, date);
        assertEquals("2017-04-26T00:00:00" + offset, isoDate);
    }

    @Test
    public void testFormatIsoSqlTimestamp() throws Exception {
        final Date date = new SimpleDateFormat(STANDARD_TIMESTAMP_FORMAT).parse("26.04.2017 11:51:31");
        final Timestamp timestamp = new Timestamp(date.getTime());
        final String isoDate = ElasticDateUtils.formatIso(timestamp, timeZone);
        final String offset = ElasticDateUtils.getOffset(timeZone, date);
        assertEquals("2017-04-26T11:51:31" + offset, isoDate);
    }

    @Test
    public void testParseIso() throws Exception {
        final Date offsetDate = DateUtils.parseDate("11.05.2017 11:27:15", STANDARD_TIMESTAMP_FORMAT);
        final String offset = ElasticDateUtils.getOffset(timeZone, offsetDate);
        final String dateStr = "2017-05-11T11:27:15" + offset;
        final Date date = ElasticDateUtils.parseIso(dateStr);
        assertEquals("11.05.2017 11:27:15", new SimpleDateFormat(STANDARD_TIMESTAMP_FORMAT).format(date));
    }
}
