/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.parameter;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Representation of a search result sort option
 */
public class SortOption {

    public enum Direction {ASC, DESC}

    private String fieldName;
    private Direction sortDirection = Direction.ASC;

    /**
     * Default constructor
     */
    public SortOption() {
    }

    /**
     * Constructor
     * @param fieldName Field name
     */
    public SortOption(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Constructor
     * @param fieldName Field name
     * @param sortDirection Sort direction
     */
    public SortOption(String fieldName, Direction sortDirection) {
        this.fieldName = fieldName;
        this.sortDirection = sortDirection;
    }

    /**
     * Gets the field name
     * @return Field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name
     * @param fieldName Field name
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the sort direction
     * @return Sort direction
     */
    public Direction getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the sort direction
     * @param sortDirection Sort direction
     */
    public void setSortDirection(Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("fieldName", fieldName)
                .append("sortDirection", sortDirection)
                .toString();
    }
}
