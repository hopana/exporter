package com.saber.tables;

/**
 * Created by Saber on 2018/5/28.
 */
public class TableEntity {

    /**
     * 字段名称
     */
    private String columnName;
    /**
     * 字段类型
     */
    private String dataType;
    /**
     * 长度
     */
    private String dataLength;
    /**
     *
     */
    private String dataPrecision;
    /**
     *
     */
    private String dataScale;
    /**
     * 是否为空
     */
    private String nullable;
    /**
     * 默认值
     */
    private String dataDefault;
    /**
     * 字段注释
     */
    private String comments;

    /**
     * 表注释
     */
    private String tableComment;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataLength() {
        return dataLength;
    }

    public void setDataLength(String dataLength) {
        this.dataLength = dataLength;
    }

    public String getDataPrecision() {
        return dataPrecision;
    }

    public void setDataPrecision(String dataPrecision) {
        this.dataPrecision = dataPrecision;
    }

    public String getDataScale() {
        return dataScale;
    }

    public void setDataScale(String dataScale) {
        this.dataScale = dataScale;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public String getDataDefault() {
        return dataDefault;
    }

    public void setDataDefault(String dataDefault) {
        this.dataDefault = dataDefault;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TableEntity{");
        sb.append("columnName='").append(columnName).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", dataLength='").append(dataLength).append('\'');
        sb.append(", dataPrecision='").append(dataPrecision).append('\'');
        sb.append(", dataScale='").append(dataScale).append('\'');
        sb.append(", nullable='").append(nullable).append('\'');
        sb.append(", dataDefault='").append(dataDefault).append('\'');
        sb.append(", comments='").append(comments).append('\'');
        sb.append(", tableComment='").append(tableComment).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
