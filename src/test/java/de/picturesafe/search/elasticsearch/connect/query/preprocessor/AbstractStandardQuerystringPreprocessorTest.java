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
package de.picturesafe.search.elasticsearch.connect.query.preprocessor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class AbstractStandardQuerystringPreprocessorTest {

    protected StandardQuerystringPreprocessor preprocessor;

    @Test
    public void testNoPhrase() {
        preprocessor.setAutoBracket(false);
        preprocessor.setInsertMissingOperators(false);

        String query = "term1,term2";
        String processedQuery = preprocessor.process(query);
        assertEquals("term1 || term2", processedQuery);

        query = "term1 , term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || term2", processedQuery);

        query = "term1 | term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || term2", processedQuery);

        query = "term1 oder term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || term2", processedQuery);

        query = "term1 or term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || term2", processedQuery);

        query = "term1 & term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2", processedQuery);

        query = "term1 + term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2", processedQuery);

        query = "term1 +term2";
        processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "term1+term2";
        processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "term1 und term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2", processedQuery);

        query = "term1 and term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2", processedQuery);

        query = "(term1 & term2) | term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2) || term3", processedQuery);

        query = "term1 - term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 NOT term2", processedQuery);

        query = "term1 -term2";
        processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "term1-term2";
        processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "term1 nicht term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 NOT term2", processedQuery);

        query = "term1 not term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 NOT term2", processedQuery);

        query = "term1/term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 term2", processedQuery);

        query = "term1[term2]";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 term2 ", processedQuery);

        query = "term1{term2}";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 term2 ", processedQuery);

        query = "term1^term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 term2", processedQuery);
    }

    @Test
    public void testPhrase() {
        preprocessor.setAutoBracket(false);
        preprocessor.setInsertMissingOperators(false);

        String query = "\"term1,term2\"";
        String processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "\"term1,term2\",term3";
        processedQuery = preprocessor.process(query);
        assertEquals("\"term1,term2\" || term3", processedQuery);

        query = "term0 & \"term1,term2\",term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term0 && \"term1,term2\" || term3", processedQuery);

        query = "\"term1,term2\",\"term3,term4\"";
        processedQuery = preprocessor.process(query);
        assertEquals("\"term1,term2\" || \"term3,term4\"", processedQuery);
    }

    @Test
    public void testPhraseWithOptimizers() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(true);

        String query = "\"term1,term2\",term3";
        String processedQuery = preprocessor.process(query);
        assertEquals("\"term1,term2\" || term3", processedQuery);

        query = "term0 & \"term1,term2\",term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term0 && \"term1,term2\") || term3", processedQuery);

        query = "\"term1,term2\",\"term3,term4\"";
        processedQuery = preprocessor.process(query);
        assertEquals("\"term1,term2\" || \"term3,term4\"", processedQuery);

        query = "\"term1 term2\"~1";
        processedQuery = preprocessor.process(query);
        assertEquals("\"term1 term2\"~1", processedQuery);
    }

    @Test
    public void testPhraseWithEscape() {
        preprocessor.setAutoBracket(false);
        preprocessor.setInsertMissingOperators(false);

        String query = "\"term1\\\"term2,term3\"";
        String processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "\"term1\\\"term2,term3\",term4";
        processedQuery = preprocessor.process(query);
        assertEquals("\"term1\\\"term2,term3\" || term4", processedQuery);
    }

    @Test
    public void testUnclosedPhrase() {
        preprocessor.setAutoBracket(false);
        preprocessor.setInsertMissingOperators(false);

        String query = "\"term1 term2";
        String processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);

        query = "\"term1 AND term2";
        processedQuery = preprocessor.process(query);
        assertEquals(query, processedQuery);
    }

    @Test
    public void testInsertMissingOperators() {
        preprocessor.setAutoBracket(false);
        preprocessor.setInsertMissingOperators(true);

        String query = "term1 term2";
        String processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2", processedQuery);

        query = "term1 term2 OR term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2 || term3", processedQuery);

        query = "term1 AND term2 term3 OR term4";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2 && term3 || term4", processedQuery);

        query = " term1 ";
        processedQuery = preprocessor.process(query);
        assertEquals(" term1 ", processedQuery);

        query = " term1 term2 ";
        processedQuery = preprocessor.process(query);
        assertEquals(" term1 && term2 ", processedQuery);
    }

    @Test
    public void testAutoBracket() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(false);

        String query = "term1 AND term2";
        String processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2)", processedQuery);

        query = "term1 AND term2 AND term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2 && term3)", processedQuery);

        query = "term1 OR term2 AND term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (term2 && term3)", processedQuery);

        query = "term1 AND term2 OR term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2) || term3", processedQuery);

        query = "term1 OR term2 AND term3 AND term4";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (term2 && term3 && term4)", processedQuery);

        query = "term1 OR term2 AND term3 AND term4 OR term5";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (term2 && term3 && term4) || term5", processedQuery);

        query = "term1 AND term2 OR term3 AND term4";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2) || (term3 && term4)", processedQuery);

        query = "term1 AND term2 OR term3 AND term4 OR term5";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2) || (term3 && term4) || term5", processedQuery);

        query = "term1 OR term2 AND term3 OR term4 AND term5";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (term2 && term3) || (term4 && term5)", processedQuery);

        query = "term1 term2 AND term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 (term2 && term3)", processedQuery);

        query = "term1 AND term2 term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2) term3", processedQuery);

        query = "term1 term2 AND term3 term4";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 (term2 && term3) term4", processedQuery);

        query = "\"term1 OR term2 AND term3\"";
        processedQuery = preprocessor.process(query);
        assertEquals("\"term1 OR term2 AND term3\"", processedQuery);
    }

    @Test
    public void testAutoBracketWithNot() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(false);

        String query = "term1 AND NOT term2";
        String processedQuery = preprocessor.process(query);
        assertEquals("(term1 && (NOT term2))", processedQuery);

        query = "term1 OR NOT term2";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (NOT term2)", processedQuery);

        query = "NOT term1 AND term2";
        processedQuery = preprocessor.process(query);
        assertEquals("((NOT term1) && term2)", processedQuery);

        query = "NOT term1 OR term2";
        processedQuery = preprocessor.process(query);
        assertEquals("(NOT term1) || term2", processedQuery);

        query = "term1 OR NOT term2 AND term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || ((NOT term2) && term3)", processedQuery);

        query = "term1 AND NOT term2 OR term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && (NOT term2)) || term3", processedQuery);

        query = "term1 AND NOT term2 AND term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && (NOT term2) && term3)", processedQuery);

        query = "term1 OR term2 AND NOT term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (term2 && (NOT term3))", processedQuery);

        query = "term1 OR term2 NOT term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || term2 (NOT term3)", processedQuery);
    }

    @Test
    public void testAutoBracketAndInsertMissingOperators() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(true);

        String query = "term1 term2";
        String processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2)", processedQuery);

        query = "term1 OR term2 NOT term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 || (term2 && (NOT term3))", processedQuery);

        query = "term1 term2 NOT term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2 && (NOT term3))", processedQuery);

        query = "term1 AND term2 term3 OR NOT term4";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2 && term3) || (NOT term4)", processedQuery);

        query = "term1 term2~";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && term2~)", processedQuery);

        query = "term1~ AND term2";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1~ && term2)", processedQuery);
    }

    @Test
    public void testAutoBracketWithManualBrackets() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(false);

        String query = "(term1 || term2)";
        String processedQuery = preprocessor.process(query);
        assertEquals("(term1 || term2)", processedQuery);

        query = "term1 AND (term2 || term3)";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && (term2 || term3)", processedQuery);

        query = "(term1 || term2) AND term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 || term2) && term3", processedQuery);
    }

    @Test
    public void testAutoBracketWithIncorrectQueries() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(false);

        String query = "AND term1";
        String processedQuery = preprocessor.process(query);
        assertEquals("&& term1", processedQuery);

        query = "AND term1 AND term2";
        processedQuery = preprocessor.process(query);
        assertEquals("&& (term1 && term2)", processedQuery);

        query = "term1 AND";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 &&", processedQuery);

        query = "term1 AND term2 AND";
        processedQuery = preprocessor.process(query);
        assertEquals("term1 && term2 &&", processedQuery);
    }

    @Test
    public void testEscaping() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(true);

        String query = "vw up\\!";
        String processedQuery = preprocessor.process(query);
        assertEquals("(vw && up\\!)", processedQuery);

        query = "NDR \\- DAS\\! am Nachmittag";
        processedQuery = preprocessor.process(query);
        assertEquals("(NDR && \\- && DAS\\! && am && Nachmittag)", processedQuery);

        query = "term1 \\- term2 term3";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && \\- && term2 && term3)", processedQuery);

        query = "term1\\-term2\\-term3";
        processedQuery = preprocessor.process(query);
        assertEquals("term1\\-term2\\-term3", processedQuery);
    }

    @Test
    public void testEscape() {
        preprocessor.setAutoBracket(true);
        preprocessor.setInsertMissingOperators(true);

        String query = "term1\\: term2";
        String processedQuery = preprocessor.process(query);
        assertEquals("(term1\\: && term2)", processedQuery);

        query = "term1 \\:term2,term3\\\"";
        processedQuery = preprocessor.process(query);
        assertEquals("(term1 && \\:term2) || term3\\\"", processedQuery);
    }
}
