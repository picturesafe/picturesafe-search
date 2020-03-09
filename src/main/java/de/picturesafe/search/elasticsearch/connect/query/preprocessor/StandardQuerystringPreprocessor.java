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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StandardQuerystringPreprocessor implements QuerystringPreprocessor {

    static final String PHRASE_DELIMITER = "\"";
    static final String ESCAPE_CHARACTER = "\\";
    static final Locale REPLACEMENT_LOCALE = Locale.GERMAN;

    static final String TOKEN_OR = "||";
    static final String TOKEN_AND = "&&";
    static final String TOKEN_NOT = "NOT";
    static final String TOKEN_NEAR_BY = "~";

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardQuerystringPreprocessor.class);
    private static final List<String> REPLACABLE_SEPARATORS = Arrays.asList("/", "{", "}", "[", "]", "^");

    @Value("${elasticsearch.querystring_preprocessor.enabled:true}")
    private boolean enabled = true;

    @Value("${elasticsearch.querystring_preprocessor.auto_bracket:true}")
    private boolean autoBracket = true;

    @Value("${elasticsearch.querystring_preprocessor.insert_missing_operators:true}")
    private boolean insertMissingOperators = true;

    @Value("${elasticsearch.querystring_preprocessor.token_delimiters:, \"(){}[]:=\\/^~}")
    private String tokenDelimiters = ", \"(){}[]:=\\/^~";

    @Value("#{'${elasticsearch.querystring_preprocessor.synonyms.AND:and und & +}'.split(' ')}")
    private List<String> synonymsForAnd = Arrays.asList("and", "und", "&", "+");

    @Value("#{'${elasticsearch.querystring_preprocessor.synonyms.OR:or oder | ,}'.split(' ')}")
    private List<String> synonymsForOr = Arrays.asList("or", "oder", "|", ",");

    @Value("#{'${elasticsearch.querystring_preprocessor.synonyms.NOT:not nicht -}'.split(' ')}")
    private List<String> synonymsForNot = Arrays.asList("not", "nicht", "-");

    private final Lock lock = new ReentrantLock();
    private volatile Map<String, String> replacements;

    private final AutoBracketOptimizer autoBracketOptimizer = new AutoBracketOptimizer();
    private final DefaultOperatorOptimizer defaultOperatorOptimizer;

    public StandardQuerystringPreprocessor(QueryConfiguration queryConfiguration) {
        final String defaultOperator = (queryConfiguration.getDefaultQueryStringOperator() == Operator.OR) ? TOKEN_OR : TOKEN_AND;
        defaultOperatorOptimizer =  new DefaultOperatorOptimizer(defaultOperator);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAutoBracket(boolean autoBracket) {
        this.autoBracket = autoBracket;
    }

    public void setInsertMissingOperators(boolean insertMissingOperators) {
        this.insertMissingOperators = insertMissingOperators;
    }

    public void setReplacements(Map<String, String> replacements) {
        this.replacements = replacements;
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
        final String result = toString(context.tokens);
        LOGGER.debug("{}: {} -> {}", this, query, result);
        return result;
    }

    private void ensureInitialized() {
        if (replacements == null) {
            lock.lock();
            try {
                if (replacements == null) {
                    replacements = new HashMap<>();
                    synonymsForAnd.forEach(s -> replacements.put(s, TOKEN_AND));
                    synonymsForOr.forEach(s -> replacements.put(s, TOKEN_OR));
                    synonymsForNot.forEach(s -> replacements.put(s, TOKEN_NOT));
                    REPLACABLE_SEPARATORS.stream().filter(s -> tokenDelimiters.contains(s)).forEach(s -> replacements.put(s, " "));
                }
            } finally {
                lock.unlock();
            }
        }
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
        context.tokens = normalizeTokens(context.tokens);

        if (insertMissingOperators) {
            context.tokens = defaultOperatorOptimizer.optimize(context.tokens);
        }
        if (autoBracket) {
            context.tokens = autoBracketOptimizer.optimize(context.tokens);
        }
    }

    private List<String> normalizeTokens(List<String> tokens) {
        final ArrayList<String> result = new ArrayList<>(tokens.size());

        StringBuilder joinedTokens = new StringBuilder();
        String previousToken = " ";
        String nextToken;
        for (int i = 0; i < tokens.size(); i++) {
            final String token = tokens.get(i);
            if (isBinaryOperator(token)) {
                if (joinedTokens.length() > 0) {
                    result.add(joinedTokens.toString());
                    joinedTokens = new StringBuilder();
                }
                if (!previousToken.endsWith(" ")) {
                    result.add(" ");
                }
                result.add(token);
                nextToken = (i < tokens.size() - 1) ? tokens.get(i + 1) : " ";
                if (!nextToken.startsWith(" ")) {
                    result.add(" ");
                }
            } else if (StringUtils.isBlank(token) || isBracket(token)) {
                if (joinedTokens.length() > 0) {
                    result.add(joinedTokens.toString());
                    joinedTokens = new StringBuilder();
                }
                result.add(token);
            } else {
                joinedTokens.append(token);
            }
            previousToken = token;
        }

        if (joinedTokens.length() > 0) {
            result.add(joinedTokens.toString());
        }

        return result;
    }

    private boolean isBinaryOperator(String token) {
        return token.equals(TOKEN_AND) || token.equals(TOKEN_OR);
    }

    private boolean isBracket(String token) {
        return token.equals("(") || token.equals(")");
    }

    private String toString(List<String> tokens) {
        final StringBuilder result = new StringBuilder();
        for (final String token : tokens) {
            result.append(token);
        }
        return result.toString();
    }

    private static class PreprocessorContext {
        List<String> tokens = new ArrayList<>();
        StringBuilder phrase = null;
        String token;
        boolean isPhrase = false;
        boolean isEscape = false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("enabled", enabled) //--
                .append("autoBracket", autoBracket) //--
                .append("insertMissingOperators", insertMissingOperators) //--
                .append("tokenDelimiters", tokenDelimiters) //--
                .append("synonymsForAnd", synonymsForAnd) //--
                .append("synonymsForOr", synonymsForOr) //--
                .append("synonymsForNot", synonymsForNot) //--
                .toString();
    }
}
