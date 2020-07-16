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

package de.picturesafe.search.parameter;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Definition of a script sort option
 */
public class ScriptSortOption extends SortOption {

    private final ScriptDefinition scriptDefinition;

    /**
     * Creates a script sort option with ascending direction.
     *
     * @param scriptDefinition Definition of the script
     * @return ScriptSortOption
     */
    public static ScriptSortOption asc(ScriptDefinition scriptDefinition) {
        return new ScriptSortOption(scriptDefinition, Direction.ASC);
    }

    /**
     * Creates a script sort option with descending direction.
     *
     * @param scriptDefinition Definition of the script
     * @return ScriptSortOption
     */
    public static ScriptSortOption desc(ScriptDefinition scriptDefinition) {
        return new ScriptSortOption(scriptDefinition, Direction.ASC);
    }

    private ScriptSortOption(ScriptDefinition scriptDefinition, Direction sortDirection) {
        super(sortDirection);
        this.scriptDefinition = scriptDefinition;
    }

    /**
     * Gets the script definition.
     *
     * @return Script definition
     */
    public ScriptDefinition getScriptDefinition() {
        return scriptDefinition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .appendSuper(super.toString())
                .append("scriptDefinition", scriptDefinition) //--
                .toString();
    }
}
