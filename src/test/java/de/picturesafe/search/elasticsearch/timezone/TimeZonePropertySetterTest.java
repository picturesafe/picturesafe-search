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

package de.picturesafe.search.elasticsearch.timezone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TimeZonePropertySetter.class, TimeZonePropertySetterTest.Config.class}, loader = AnnotationConfigContextLoader.class)
public class TimeZonePropertySetterTest implements TimeZoneAware {

    static final String TIME_ZONE = "Europe/Vatican";

    @Test
    public void test() {
        assertEquals(TIME_ZONE, System.getProperty(TIME_ZONE_PROPERTY_KEY));
    }

    @Configuration
    static class Config {

        @Bean
        public String elasticsearchTimeZone() {
            return TIME_ZONE;
        }
    }
}
