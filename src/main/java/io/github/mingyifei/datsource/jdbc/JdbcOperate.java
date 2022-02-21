/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.mingyifei.datsource.jdbc;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.Lists;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description TODO
 * @Author ming.yifei
 * @Date 2021/12/17 10:52 下午
 **/
@Slf4j
public class JdbcOperate {

    @Data(staticConstructor = "of")
    public static class TableInfo {
        private final String catalogName;
        private final String schemaName;
        private final String tableName;
    }

    @Data(staticConstructor = "of")
    public static class ColumnInfo {
        private final TableInfo tableInfo;
        private final String name;
        private final int type;
        private final String typeName;
        private final int position;
    }

    @Data(staticConstructor = "of")
    public static class TableDefinition {
        private final TableInfo tableInfo;
        private final List<ColumnInfo> columns;
        private final List<ColumnInfo> keyColumns;
    }

    public static TableInfo getTableInfo(Connection connection, String tableName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rs = metadata.getTables(connection.getCatalog(), "%", tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String gotTableName = rs.getString("TABLE_NAME");
                checkState(tableName.equals(gotTableName), "表不存在：" + tableName);
                return TableInfo.of(connection.getCatalog(), schemaName, tableName);
            } else {
                throw new Exception("表不存在: " + tableName);
            }
        }
    }

    public static String getPrimaryKeys(Connection connection, String tableName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rs = metadata.getPrimaryKeys(connection.getCatalog(), "%", tableName)) {
            if (rs.next()) {
                return rs.getString("column_name");
            } else {
                throw new Exception("主键不存在: " + tableName);
            }
        }
    }

    public static TableDefinition getTableDefinition(Connection connection, TableInfo tableInfo) throws Exception {
        TableDefinition table = TableDefinition.of(
                tableInfo, Lists.newArrayList(), Lists.newArrayList());

        try (ResultSet rs = connection.getMetaData().getColumns(
                tableInfo.getCatalogName(),
                tableInfo.getSchemaName(),
                tableInfo.getTableName(),
                "%"
        )) {
            while (rs.next()) {
                final String columnName = rs.getString("COLUMN_NAME");
                final int sqlDataType = rs.getInt("DATA_TYPE");
                final String typeName = rs.getString("TYPE_NAME");
                final int position = rs.getInt("ORDINAL_POSITION");

                ColumnInfo columnInfo = ColumnInfo.of(tableInfo, columnName, sqlDataType, typeName, position);
                table.columns.add(columnInfo);
            }
            return table;
        }
    }

    public static String buildInsertSql(TableDefinition table) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ");
        builder.append(table.tableInfo.getTableName());
        builder.append("(");

        table.columns.forEach(columnInfo -> builder.append(columnInfo.getName()).append(","));
        builder.deleteCharAt(builder.length() - 1);

        builder.append(") values(");
        IntStream.range(0, table.columns.size() - 1).forEach(i -> builder.append("?,"));
        builder.append("?)");

        return builder.toString();
    }

    public static PreparedStatement buildInsertStatement(Connection connection, String insertSQL) throws SQLException {
        return connection.prepareStatement(insertSQL);
    }

    public static String combationWhere(List<ColumnInfo> columnInfos) {
        StringBuilder builder = new StringBuilder();
        if (!columnInfos.isEmpty()) {
            builder.append(" where ");
            StringJoiner whereJoiner = new StringJoiner(" AND ");
            columnInfos.forEach((columnInfo -> {
                StringJoiner equals = new StringJoiner("=");
                equals.add(columnInfo.getName()).add("?");
                whereJoiner.add(equals.toString());
            }));
            builder.append(whereJoiner);
            return builder.toString();
        }
        return "";
    }

    public static String buildQuerySql(TableDefinition table) {
        return "select * from "
                + table.tableInfo.getTableName();
    }

    public static String buildQueryCreateSql(String tableName) {
        return "show create table "
                + tableName;
    }
    public static String buildShowTableSql() {
        return "show tables";
    }

    public static String buildDeleteSql(TableDefinition table) {
        return "delete from "
                + table.tableInfo.getTableName()
                + combationWhere(table.keyColumns);
    }

    public static PreparedStatement buildDeleteStatement(Connection connection, String deleteSQL) throws SQLException {
        return connection.prepareStatement(deleteSQL);
    }

    public static String getDriverClassName(String jdbcUrl) throws Exception {
        return Arrays.stream(JdbcDriverType.values())
                .filter(jdbcDriverType -> jdbcDriverType.matches(jdbcUrl))
                .findFirst()
                .orElseThrow(() -> new Exception("JDBC connection包含未知驱动: " + jdbcUrl))
                .getDriverClass();
    }
}
