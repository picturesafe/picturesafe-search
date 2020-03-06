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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OptimizerAutoBracket implements QueryTokenOptimizer {

    private enum OptimizeState {TOKEN, OPERATOR_AND, OPERATOR_NOT}

    @Override
    public List<String> optimize(List<String> tokens) {
        if (!QueryOptimizerUtils.containsBrackets(tokens)) {
            final OptimizeContext context = new OptimizeContext(tokens);
            context.beginNewState(OptimizeState.TOKEN);

            for (context.tokensIndex = 0; context.tokensIndex < tokens.size(); context.tokensIndex++) {
                context.currenToken = context.tokens.get(context.tokensIndex);

                switch (context.currenToken) {
                    case StandardQuerystringPreprocessor.TOKEN_NOT:
                        processNotOperator(context);
                        break;
                    case StandardQuerystringPreprocessor.TOKEN_AND:
                        processAndOperator(context);
                        break;
                    default:
                        processToken(context);
                }
            }

            while (context.endCurrentState() != OptimizeState.TOKEN) {
                if (currentTokenIsOperator(context)) {
                    removePreviousOpeningBracket(context);
                } else {
                    closeBracket(context);
                }
            }

            return context.result;
        } else {
            return tokens;
        }
    }

    private void processNotOperator(OptimizeContext context) {
        context.beginNewState(OptimizeState.OPERATOR_NOT);
        openBracket(context);
        addCurrentToken(context);
    }

    private void processAndOperator(OptimizeContext context) {
        if (context.tokensIndex > 0 && context.getCurrentState() != OptimizeState.OPERATOR_AND) {
            context.beginNewState(OptimizeState.OPERATOR_AND);
            openBracket(context);
        }
        addCurrentToken(context);
    }

    private void processToken(OptimizeContext context) {
        addCurrentToken(context);
        if (StringUtils.isNotBlank(context.currenToken)) {
            if (context.getCurrentState() == OptimizeState.OPERATOR_NOT) {
                closeBracket(context);
                context.endCurrentState();
            }
            if (context.getCurrentState() == OptimizeState.OPERATOR_AND && !followingTokenIsBracketBinding(context)) {
                closeBracket(context);
                context.endCurrentState();
            }
        }
    }

    private void addCurrentToken(OptimizeContext context) {
        context.result.add(context.currenToken);
        context.resultIndex++;
    }

    private void openBracket(OptimizeContext context) {
        final int index = (context.getCurrentState() == OptimizeState.OPERATOR_AND)
                ? findPreviousRelevantToken(context.result, context.resultIndex - 1)
                : context.resultIndex;
        if (index > -1) {
            context.result.add(index, "(");
            context.resultIndex++;
            context.openBracketCount++;
        }
    }

    private void closeBracket(OptimizeContext context) {
        if (context.openBracketCount > 0) {
            context.result.add(")");
            context.resultIndex++;
            context.openBracketCount--;
        }
    }

    private int findPreviousRelevantToken(List<String> tokens, int startIndex) {
        boolean isBracketTerm = false;
        for (int i = startIndex; i >= 0; i--) {
            final String token = tokens.get(i);

            if (StringUtils.isNotBlank(token)) {
                if (token.equals(")")) {
                    isBracketTerm = true;
                } else if (token.equals("(")) {
                    isBracketTerm = false;
                }

                if (!isBracketTerm && !isBracketBindingToken(token)) {
                    return i;
                }
            }
        }
        return 0;
    }

    private boolean isBracketBindingToken(String token) {
        return token.equals(StandardQuerystringPreprocessor.TOKEN_AND) || token.equals(StandardQuerystringPreprocessor.TOKEN_NEAR_BY);
    }

    private boolean followingTokenIsBracketBinding(OptimizeContext context) {
        final List<String> followingTokens = getNextNonBlankTokens(context);
        return isBracketBindingToken(lastToken(followingTokens));
    }

    private List<String> getNextNonBlankTokens(OptimizeContext context) {
        final List<String> result = new ArrayList<>();
        for (int i = context.tokensIndex + 1; i < context.tokens.size(); i++) {
            final String token = context.tokens.get(i);
            result.add(token);
            if (StringUtils.isNotBlank(token)) {
                break;
            }
        }
        return result;
    }

    private String lastToken(List<String> tokens) {
        if (CollectionUtils.isNotEmpty(tokens)) {
            return tokens.get(tokens.size() - 1);
        } else {
            return "";
        }
    }

    private boolean currentTokenIsOperator(OptimizeContext context) {
        switch (context.currenToken) {
            case StandardQuerystringPreprocessor.TOKEN_AND:
            case StandardQuerystringPreprocessor.TOKEN_OR:
            case StandardQuerystringPreprocessor.TOKEN_NOT:
                return true;
            default:
                return false;
        }
    }

    private void removePreviousOpeningBracket(OptimizeContext context) {
        for (int i = context.resultIndex - 1; i >= 0; i--) {
            final String token = context.result.get(i);
            if (token.equals("(")) {
                context.result.remove(i);
            }
        }
    }

    private static class OptimizeContext {
        final List<String> tokens;
        final Stack<OptimizeState> states = new Stack<>();

        String currenToken;
        final List<String> result = new ArrayList<>();
        int tokensIndex;
        int resultIndex;
        int openBracketCount;

        OptimizeContext(List<String> tokens) {
            this.tokens = tokens;
        }

        void beginNewState(OptimizeState state) {
            states.push(state);
        }

        OptimizeState getCurrentState() {
            return states.peek();
        }

        OptimizeState endCurrentState() {
            return (states.size() > 1) ? states.pop() : states.peek();
        }
    }
}
