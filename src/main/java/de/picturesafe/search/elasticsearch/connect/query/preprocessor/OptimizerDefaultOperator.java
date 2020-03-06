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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OptimizerDefaultOperator implements QueryTokenOptimizer {

    private enum OptimizeState {TOKEN, OPERATOR_AND, OPERATOR_OTHER, NONE}

    private final String defaultOperator;

    public OptimizerDefaultOperator(String defaultOperator) {
        this.defaultOperator = defaultOperator;
    }

    @Override
    public List<String> optimize(List<String> tokens) {
        if (!QueryOptimizerUtils.containsBrackets(tokens)) {
            final OptimizeContext context = new OptimizeContext(tokens);

            for (context.tokensIndex = 0; context.tokensIndex < tokens.size(); context.tokensIndex++) {
                context.currenToken = context.tokens.get(context.tokensIndex);

                switch (context.currenToken) {
                    case StandardQuerystringPreprocessor.TOKEN_NOT:
                        processNotOperator(context);
                        break;
                    case StandardQuerystringPreprocessor.TOKEN_AND:
                        processAndOperator(context);
                        break;
                    case StandardQuerystringPreprocessor.TOKEN_OR:
                    case StandardQuerystringPreprocessor.TOKEN_NEAR_BY:
                        processOtherOperator(context);
                        break;
                    default:
                        processToken(context);
                }
            }

            return context.result;
        } else {
            return tokens;
        }
    }

    private void processNotOperator(OptimizeContext context) {
        if (context.tokensIndex > 0 && context.state == OptimizeState.TOKEN) {
            addToken(context, defaultOperator);
            addToken(context, " ");
        }
        context.state = OptimizeState.OPERATOR_OTHER;
        addCurrentToken(context);
    }

    private void processAndOperator(OptimizeContext context) {
        context.state = OptimizeState.OPERATOR_AND;
        addCurrentToken(context);
    }

    private void processOtherOperator(OptimizeContext context) {
        context.state = OptimizeState.OPERATOR_OTHER;
        addCurrentToken(context);
    }

    private void processToken(OptimizeContext context) {
        if (StringUtils.isNotBlank(context.currenToken)) {
            if (context.state == OptimizeState.TOKEN) {
                addToken(context, defaultOperator);
                addToken(context, " ");
            }
            context.state = OptimizeState.TOKEN;
        }
        addCurrentToken(context);
    }

    private void addCurrentToken(OptimizeContext context) {
        addToken(context, context.currenToken);
    }

    private void addToken(OptimizeContext context, String token) {
        context.result.add(token);
        context.resultIndex++;
    }

    private static class OptimizeContext {
        final List<String> tokens;

        OptimizeState state = OptimizeState.NONE;
        String currenToken;
        final List<String> result = new ArrayList<>();
        int tokensIndex;
        int resultIndex;

        OptimizeContext(List<String> tokens) {
            this.tokens = tokens;
        }
    }
}
