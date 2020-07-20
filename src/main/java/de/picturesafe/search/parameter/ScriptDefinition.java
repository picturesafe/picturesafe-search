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

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of a native script which will be executed in Elasticsearch
 */
public class ScriptDefinition {

    public enum ScriptType {INLINE, STORED}
    public enum SortType {STRING, NUMBER}

    public static final SortType DEFAULT_SORT_TYPE = SortType.NUMBER;
    public static final String DEFAULT_LANGUAGE = "painless";

    private final ScriptType scriptType;
    private final String idOrCode;

    private String language = DEFAULT_LANGUAGE;
    private SortType sortType = DEFAULT_SORT_TYPE;
    private Map<String, String> options = new HashMap<>();
    private Map<String, Object> params = new HashMap<>();

    /**
     * Creates a new inline script definition.
     *
     * @param code Script code
     * @return ScriptDefinition
     */
    public static ScriptDefinition inline(String code) {
        return new ScriptDefinition(ScriptType.INLINE, code);
    }

    /**
     * Creates a new script definition referring to a stored script.
     *
     * @param id ID of the stored script
     * @return ScriptDefinition
     */
    public static ScriptDefinition stored(String id) {
        return new ScriptDefinition(ScriptType.STORED, id);
    }

    private ScriptDefinition(ScriptType scriptType, String idOrCode) {
        this.scriptType = scriptType;
        this.idOrCode = idOrCode;
    }

    /**
     * Gets the script type.
     *
     * @return Script type
     */
    public ScriptType getScriptType() {
        return scriptType;
    }

    /**
     * Gets the ID or code of the script.
     *
     * @return ID or code
     */
    public String getIdOrCode() {
        return idOrCode;
    }

    /**
     * Gets the script language.
     *
     * @return Script language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the script language.
     *
     * @param language Script language
     * @return ScriptDefinition
     */
    public ScriptDefinition language(String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets the sort type.
     *
     * @return Sort Type
     */
    public SortType getSortType() {
        return sortType;
    }

    /**
     * Sets the sort type.
     *
     * @param sortType Sort type
     * @return ScriptDefinition
     */
    public ScriptDefinition sortType(SortType sortType) {
        this.sortType = sortType;
        return this;
    }

    /**
     * Gets the script options.
     *
     * @return Script options
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Sets the script options.
     *
     * @param options Script options
     * @return ScriptDefinition
     */
    public ScriptDefinition options(Map<String, String> options) {
        this.options = options;
        return this;
    }

    /**
     * Adds a script option.
     *
     * @param name  Option name
     * @param value Option value
     * @return ScriptDefinition
     */
    public ScriptDefinition option(String name, String value) {
        options.put(name, value);
        return this;
    }

    /**
     * Gets the script parameters.
     *
     * @return Script parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Sets the script parameters.
     *
     * @param params Script parameters
     * @return ScriptDefinition
     */
    public ScriptDefinition params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /**
     * Adds a script parameter.
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @return ScriptDefinition
     */
    public ScriptDefinition param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("scriptType", scriptType) //--
                .append("idOrCode", idOrCode) //--
                .append("language", language) //--
                .append("sortType", sortType) //--
                .append("options", options) //--
                .append("params", params) //--
                .toString();
    }
}
