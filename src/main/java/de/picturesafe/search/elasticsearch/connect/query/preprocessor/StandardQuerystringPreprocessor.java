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

import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import de.picturesafe.search.elasticsearch.connect.query.QuerystringPreprocessor;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.Operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class StandardQuerystringPreprocessor implements QuerystringPreprocessor {

    static final String PHRASE_DELIMITER = "\"";
    static final String ESCAPE_CHARACTER = "\\";
    static final Locale REPLACEMENT_LOCALE = Locale.GERMAN;

    static final String TOKEN_OR = "||";
    static final String TOKEN_AND = "&&";
    static final String TOKEN_NOT = "NOT";
    static final String TOKEN_NEAR_BY = "~";

    private static final String DEFAULT_TOKEN_DELIMITERS = " ,\"(){}[]:=\\/^~";
    private static final boolean DEFAULT_AUTO_BRACKET = true;
    private static final boolean DEFAULT_INSERT_MISSING_OPERATORS = true;
    private static final Map<String, String> DEFAULT_REPLACEMENTS;

    private String tokenDelimiters = DEFAULT_TOKEN_DELIMITERS;
    private Map<String, String> replacements = DEFAULT_REPLACEMENTS;
    private boolean autoBracket = DEFAULT_AUTO_BRACKET;
    private boolean insertMissingOperators = DEFAULT_INSERT_MISSING_OPERATORS;

    private final AutoBracketOptimizer queryAutoBracketOptimizer = new AutoBracketOptimizer();
    private final DefaultOperatorOptimizer queryDefaultOperatorOptimizer;

    static {
        DEFAULT_REPLACEMENTS = new HashMap<>();
        DEFAULT_REPLACEMENTS.put(",", " " + TOKEN_OR + " ");  // Blanks are necessary, do not remove!
        DEFAULT_REPLACEMENTS.put("|", TOKEN_OR);
        DEFAULT_REPLACEMENTS.put("oder", TOKEN_OR);
        DEFAULT_REPLACEMENTS.put("or", TOKEN_OR);
        DEFAULT_REPLACEMENTS.put("&", TOKEN_AND);
        DEFAULT_REPLACEMENTS.put("+", TOKEN_AND);
        DEFAULT_REPLACEMENTS.put("und", TOKEN_AND);
        DEFAULT_REPLACEMENTS.put("and", TOKEN_AND);
        DEFAULT_REPLACEMENTS.put("-", TOKEN_NOT);
        DEFAULT_REPLACEMENTS.put("nicht", TOKEN_NOT);
        DEFAULT_REPLACEMENTS.put("not", TOKEN_NOT); // Convert lowercase "not" to uppercase "NOT" because Elasticsearch only supports uppercase.
        DEFAULT_REPLACEMENTS.put("/", " ");
        DEFAULT_REPLACEMENTS.put("{", " ");
        DEFAULT_REPLACEMENTS.put("}", " ");
        DEFAULT_REPLACEMENTS.put("[", " ");
        DEFAULT_REPLACEMENTS.put("]", " ");
        DEFAULT_REPLACEMENTS.put("^", " ");
    }

    public StandardQuerystringPreprocessor(QueryConfiguration queryConfiguration) {
        final String defaultOperator = (queryConfiguration.getDefaultQueryStringOperator() == Operator.OR) ? TOKEN_OR : TOKEN_AND;
        queryDefaultOperatorOptimizer = new DefaultOperatorOptimizer(defaultOperator);
    }

    public void setTokenDelimiters(String tokenDelimiters) {
        this.tokenDelimiters = tokenDelimiters;
    }

    public void setReplacements(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    public void setAutoBracket(boolean autoBracket) {
        this.autoBracket = autoBracket;
    }

    public void setInsertMissingOperators(boolean insertMissingOperators) {
        this.insertMissingOperators = insertMissingOperators;
    }

    public String process(String query) {
        final PreprocessorContext context = new PreprocessorContext();

        for (final StringTokenizer st = new StringTokenizer(query, tokenDelimiters, true); st.hasMoreTokens();) {
            context.token = st.nextToken();
            final String normalizedToken = context.token.toLowerCase(REPLACEMENT_LOCALE);
            boolean tokenAlreadyAdded = false;

            if (!context.isPhrase) {
                if (context.token.equals(PHRASE_DELIMITER) && !context.isEscape) {
                    beginPhrase(context);
                } else if (!context.isEscape && replacements.containsKey(normalizedToken)) {
                    context.token = replacements.get(normalizedToken);
                }
            } else if (context.token.equals(PHRASE_DELIMITER) && !context.isEscape) {
                endPhrase(context);
            }

            if (context.isEscape && !context.isPhrase) {
                concatOrAddToken(context);
                tokenAlreadyAdded = true;
            }

            context.isEscape = context.token != null && context.token.equals(ESCAPE_CHARACTER);

            if (!tokenAlreadyAdded) {
                if (context.isPhrase) {
                    context.phrase.append(context.token);
                } else if (context.isEscape) {
                    concatOrAddToken(context);
                } else if (context.token != null) {
                    context.tokens.addAll(splitByBlanks(context.token));
                }
            }
        }

        finalizeContext(context);
        return toString(context.tokens);
    }

    private void beginPhrase(PreprocessorContext context) {
        context.phrase = new StringBuilder();
        context.isPhrase = true;
    }

    private void endPhrase(PreprocessorContext context) {
        context.phrase.append(context.token);
        context.tokens.add(context.phrase.toString());
        context.phrase = null;
        context.token = null;
        context.isPhrase = false;
    }

    private void concatOrAddToken(PreprocessorContext context) {
        final int index = context.tokens.size() - 1;
        final String lastToken = (context.tokens.size() > 0) ? context.tokens.get(index) : null;
        if (StringUtils.isNotBlank(lastToken)) {
            context.tokens.set(index, lastToken + context.token);
        } else {
            context.tokens.add(context.token);
        }
    }

    private List<String> splitByBlanks(String token) {
        final List<String> result = new ArrayList<>();
        for (final StringTokenizer st = new StringTokenizer(token, " ", true); st.hasMoreTokens();) {
            result.add(st.nextToken());
        }
        return result;
    }

    private void finalizeContext(PreprocessorContext context) {
        if (context.phrase != null) {
            context.tokens.add(context.phrase.toString());
        }
        optimizeQuery(context);
    }

    private void optimizeQuery(PreprocessorContext context) {
        context.tokens = joinTokensWithoutBlanks(context.tokens);

        if (insertMissingOperators) {
            context.tokens = queryDefaultOperatorOptimizer.optimize(context.tokens);
        }
        if (autoBracket) {
            context.tokens = queryAutoBracketOptimizer.optimize(context.tokens);
        }
    }

    private List<String> joinTokensWithoutBlanks(List<String> tokens) {
        final ArrayList<String> result = new ArrayList<>(tokens.size());

        StringBuilder joinedTokens = new StringBuilder();
        for (String token : tokens) {
            if (StringUtils.isNotBlank(token) && !(token.equals("(") | token.equals(")"))) {
                joinedTokens.append(token);
            } else {
                if (joinedTokens.length() > 0) {
                    result.add(joinedTokens.toString());
                    joinedTokens = new StringBuilder();
                }
                result.add(token);
            }
        }

        if (joinedTokens.length() > 0) {
            result.add(joinedTokens.toString());
        }

        return result;
    }

    private String toString(List<String> tokens) {
        final StringBuilder result = new StringBuilder();
        for (final String token : tokens) {
            result.append(token);
        }
        return result.toString();
    }

    private class PreprocessorContext {
        List<String> tokens = new ArrayList<>();
        StringBuilder phrase = null;
        String token;
        boolean isPhrase = false;
        boolean isEscape = false;
    }
}
