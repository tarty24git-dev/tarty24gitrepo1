package com.cth.app.model;

import java.util.List;

public class TableMetadata {
    private String tableName;
    private String tableType;
    private List<ColumnMetadata> columns;

    public TableMetadata() {}

    public TableMetadata(String tableName, String tableType, List<ColumnMetadata> columns) {
        this.tableName = tableName;
        this.tableType = tableType;
        this.columns = columns;
    }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getTableType() { return tableType; }
    public void setTableType(String tableType) { this.tableType = tableType; }

    public List<ColumnMetadata> getColumns() { return columns; }
    public void setColumns(List<ColumnMetadata> columns) { this.columns = columns; }

    public static class ColumnMetadata {
        private String columnName;
        private String dataType;
        private int columnSize;
        private boolean isNullable;

        public ColumnMetadata() {}

        public ColumnMetadata(String columnName, String dataType, int columnSize, boolean isNullable) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.columnSize = columnSize;
            this.isNullable = isNullable;
        }

        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }

        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }

        public int getColumnSize() { return columnSize; }
        public void setColumnSize(int columnSize) { this.columnSize = columnSize; }

        public boolean isNullable() { return isNullable; }
        public void setNullable(boolean nullable) { isNullable = nullable; }
    }
}
