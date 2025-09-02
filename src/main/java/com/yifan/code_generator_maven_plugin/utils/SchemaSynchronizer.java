package com.yifan.code_generator_maven_plugin.utils;

import com.yifan.code_generator_maven_plugin.common.CommonFunc;
import com.yifan.code_generator_maven_plugin.constant.Constants;
import com.yifan.code_generator_maven_plugin.model.ColumnDefinition;
import com.yifan.code_generator_maven_plugin.model.GeneratorConfig;
import com.yifan.code_generator_maven_plugin.model.JdbcConfig;
import com.yifan.code_generator_maven_plugin.model.TableConfig;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SchemaSynchronizer {

    private final GeneratorConfig generatorConfig;
    private final Log log;
    private final MavenProject project; // Made final as it's set once in constructor


    /**
     * Constructor for SchemaSynchronizer.
     *
     * @param project         The Maven project instance.
     * @param generatorConfig The generator configuration.
     * @param log             The Maven plugin logger.
     */
    public SchemaSynchronizer(MavenProject project, GeneratorConfig generatorConfig, Log log) { // Renamed constructor
        this.project = project;
        this.generatorConfig = generatorConfig;
        this.log = log;
    }

    /**
     * Synchronizes the database schema based on the provided table configurations.
     *
     * @param tables     List of table configurations to synchronize.
     * @param executeSql If true, the generated SQL statements will be executed on the database.
     * @throws SQLException           If a database access error occurs.
     * @throws ClassNotFoundException If the JDBC driver class cannot be found.
     * @throws IOException            If an I/O error occurs while saving the SQL script.
     */
    public void syncSchema(List<TableConfig> tables, boolean executeSql) throws SQLException, ClassNotFoundException, IOException {
        // Early exit if JDBC configuration is missing
        if (generatorConfig == null || generatorConfig.getBaseConfigs() == null || generatorConfig.getBaseConfigs().getJdbcConfig() == null) {
            log.warn("JDBC configuration not found, skipping database synchronization.");
            return;
        }

        JdbcConfig jdbcConfig = generatorConfig.getBaseConfigs().getJdbcConfig();

        List<String> sqlStatements = new ArrayList<>();
        String driver = jdbcConfig.getDriver();
        String url = jdbcConfig.getUrl();
        String username = jdbcConfig.getUsername();
        String password = jdbcConfig.getPassword();
        printInfoLog("Starting database schema synchronization... 🔄");
        printInfoLog("Using JDBC Driver: " + driver);
        printInfoLog("url: " + url);
        printInfoLog("username: " + username);
        // It's generally not a good idea to log passwords
        printInfoLog("password: " + password);

        // Load the JDBC driver
        Class.forName(driver);

        // Generate SQL statements
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            for (TableConfig table : tables) {
                if (!tableExists(connection, table.getTableName())) {
                    printInfoLog("Table '" + table.getTableName() + "' does not exist. Generating CREATE TABLE statement.");
                    sqlStatements.add(generateCreateTableSql(table));
                } else {
                    printInfoLog("Table '" + table.getTableName() + "' exists. Generating ALTER TABLE statements.");
                    sqlStatements.addAll(generateAlterTableSql(connection, table));
                }
            }
        }

        // Save all generated SQL statements to a single file
        saveSqlScript(sqlStatements);

        // Execute SQL statements if requested and if there are statements to execute
        if (!sqlStatements.isEmpty()) {
            if (executeSql) {
                printInfoLog("Executing schema updates on the database...");
                try (Connection connection = DriverManager.getConnection(url, username, password);
                     Statement statement = connection.createStatement()) {
                    for (String sql : sqlStatements) {
                        printInfoLog("Executing: " + sql);
                        statement.addBatch(sql);
                    }
                    statement.executeBatch();
                    printInfoLog("Database schema updated successfully. ✅");
                }
            } else {
                printInfoLog("SQL scripts generated. To apply them to the database, run with -DsyncDb=true. 📄");
            }
        } else {
            printInfoLog("No database schema changes detected. 🤷‍♂️");
        }
    }

    /**
     * Generates ALTER TABLE statements for a given table by comparing desired and existing columns.
     *
     * @param connection The database connection.
     * @param table      The table configuration.
     * @return A list of ALTER TABLE statements.
     * @throws SQLException If a database access error occurs.
     */
    private List<String> generateAlterTableSql(Connection connection, TableConfig table) throws SQLException {
        List<String> alterStatements = new ArrayList<>();
        Map<String, ColumnDefinition> existingColumns = getExistingColumns(connection, table.getTableName());
        Map<String, ColumnDefinition> desiredColumns = table.getColumns().stream()
                .collect(Collectors.toMap(c -> CommonFunc.toSnakeCase(c.getJavaName()), c -> c));

        // Filter out base columns from desired and existing lists for comparison purposes
        // Base columns are managed implicitly or always exist, so they shouldn't trigger ALTER statements
        Map<String, ColumnDefinition> filteredDesiredColumns = desiredColumns.entrySet().stream()
                .filter(entry -> !isBaseColumn(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, ColumnDefinition> filteredExistingColumns = existingColumns.entrySet().stream()
                .filter(entry -> !isBaseColumn(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        // 1. Check for new columns to add (only for non-base columns)
        filteredDesiredColumns.forEach((dbName, col) -> {
            if (!filteredExistingColumns.containsKey(dbName)) {
                generateAddColumnSql(alterStatements, table.getTableName(), col);
            }
        });

        // 2. Check for columns to drop (only for non-base columns)
        filteredExistingColumns.forEach((existingColName, existingCol) -> {
            if (!filteredDesiredColumns.containsKey(existingColName)) {
                generateDropColumnSql(alterStatements, table.getTableName(), existingColName);
            }
        });

        // 3. Check for modified columns (type or comment changes, only for non-base columns)
        filteredDesiredColumns.forEach((dbName, desiredCol) -> {
            if (filteredExistingColumns.containsKey(dbName)) {
                ColumnDefinition existingCol = filteredExistingColumns.get(dbName);
                String desiredDbType = mapJavaTypeToDbType(desiredCol.getJavaType());

                if (!desiredDbType.equalsIgnoreCase(existingCol.getDbType()) || !desiredCol.getComment().equals(existingCol.getComment())) {
                    generateModifyColumnSql(alterStatements, table.getTableName(), desiredCol, existingCol);
                }
            }
        });

        return alterStatements;
    }

    /**
     * Generates an SQL statement to add a new column.
     *
     * @param alterStatements The list to add the SQL statement to.
     * @param tableName       The name of the table.
     * @param col             The column definition.
     */
    private void generateAddColumnSql(List<String> alterStatements, String tableName, ColumnDefinition col) {
        String dbType = mapJavaTypeToDbType(col.getJavaType());
        String dbName = CommonFunc.toSnakeCase(col.getJavaName());
        alterStatements.add(String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s COMMENT '%s';",
                tableName, dbName, dbType, col.getComment()));
        printInfoLog(String.format("Generated ADD COLUMN for table '%s', column '%s'.", tableName, dbName));
    }

    /**
     * Generates an SQL statement to drop an existing column.
     * This is marked as a dangerous operation.
     *
     * @param alterStatements The list to add the SQL statement to.
     * @param tableName       The name of the table.
     * @param columnName      The name of the column to drop.
     */
    private void generateDropColumnSql(List<String> alterStatements, String tableName, String columnName) {
        log.warn(String.format(
                "Column '%s' in table '%s' exists in DB but not in config. Generating DROP statement.",
                columnName, tableName));
        // Refactored to use StringBuilder for explicit string construction, avoiding printf-style format string concerns.
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("-- DANGER: Column '").append(columnName).append("' was removed from config and will be dropped from table '").append(tableName).append("'.\n");
        sqlBuilder.append("ALTER TABLE `").append(tableName).append("` DROP COLUMN `").append(columnName).append("`;");
        alterStatements.add(sqlBuilder.toString());
    }

    /**
     * Generates an SQL statement to modify an existing column's type or comment.
     *
     * @param alterStatements The list to add the SQL statement to.
     * @param tableName       The name of the table.
     * @param desiredCol      The desired column definition.
     * @param existingCol     The existing column definition.
     */
    private void generateModifyColumnSql(List<String> alterStatements, String tableName, ColumnDefinition desiredCol, ColumnDefinition existingCol) {
        String dbName = existingCol.getDbName();
        String desiredDbType = mapJavaTypeToDbType(desiredCol.getJavaType());
        printInfoLog(String.format("Generated MODIFY COLUMN for table '%s', column '%s'. Old type: '%s', new type: '%s', old comment: '%s', new comment: '%s'.",
                tableName, dbName, existingCol.getDbType(), desiredDbType, existingCol.getComment(), desiredCol.getComment()));
        alterStatements.add(String.format("ALTER TABLE `%s` MODIFY COLUMN `%s` %s COMMENT '%s';",
                tableName, dbName, desiredDbType, desiredCol.getComment()));
    }

    /**
     * Checks if a column name is one of the predefined base columns.
     *
     * @param columnName The name of the column to check.
     * @return True if it's a base column, false otherwise.
     */
    /* package-private */ boolean isBaseColumn(String columnName) { // Changed to package-private
        return columnName != null && Constants.ColumnConstants.BASE_COLUMN_NAMES.contains(columnName.toLowerCase());
    }

    /**
     * Retrieves existing column definitions for a given table from the database.
     *
     * @param connection The database connection.
     * @param tableName  The name of the table.
     * @return A map of existing column definitions, keyed by lowercase column name.
     * @throws SQLException If a database access error occurs.
     */
    private Map<String, ColumnDefinition> getExistingColumns(Connection connection, String tableName)
            throws SQLException {
        Map<String, ColumnDefinition> columns = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
                ColumnDefinition col = new ColumnDefinition();
                String colName = rs.getString(Constants.ColumnConstants.COLUMN_NAME);
                col.setDbName(colName);
                col.setDbType(rs.getString(Constants.ColumnConstants.COLUMN_TYPE_NAME));
                col.setComment(rs.getString(Constants.ColumnConstants.COLUMN_REMARKS));
                columns.put(colName.toLowerCase(), col);
            }
        }
        return columns;
    }

    /**
     * Checks if a table exists in the database.
     *
     * @param connection The database connection.
     * @param tableName  The name of the table to check.
     * @return True if the table exists, false otherwise.
     * @throws SQLException If a database access error occurs.
     */
    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{Constants.ColumnConstants.TABLE_TYPE})) {
            return rs.next();
        }
    }

    /**
     * Generates a CREATE TABLE SQL statement for the given table configuration.
     *
     * @param table The table configuration.
     * @return The CREATE TABLE SQL statement.
     */
    private String generateCreateTableSql(TableConfig table) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(table.getTableName()).append("` (\n");
        sql.append("  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',\n"); // Primary key 'id' is always present

        for (ColumnDefinition column : table.getColumns()) {
            String dbType = mapJavaTypeToDbType(column.getJavaType());
            String dbName = CommonFunc.toSnakeCase(column.getJavaName());
            sql.append("  `").append(dbName).append("` ").append(dbType).append(" COMMENT '")
                    .append(column.getComment()).append("',\n");
        }

        // Conditionally add base entity columns
        if (table.isUseBaseEntity()) {
            sql.append("  `created_by` BIGINT COMMENT '创建人ID',\n");
            sql.append("  `created_by_name` VARCHAR(255) COMMENT '创建人名称',\n");
            sql.append("  `create_time` DATETIME COMMENT '创建时间',\n");
            sql.append("  `updated_by` BIGINT COMMENT '更新人ID',\n");
            sql.append("  `updated_by_name` VARCHAR(255) COMMENT '更新人名称',\n");
            sql.append("  `updated_time` DATETIME COMMENT '更新时间',\n");
            sql.append("  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',\n");
            sql.append("  `version` INT DEFAULT 1 COMMENT '乐观锁版本号',\n");
        }

        sql.append("  PRIMARY KEY (`id`)\n");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='").append(table.getEntityName()).append("';");
        return sql.toString();
    }

    /**
     * Maps a Java type string to its corresponding database type string using the generator configuration.
     * Defaults to "VARCHAR(255)" if no mapping is found.
     *
     * @param javaType The Java type string.
     * @return The corresponding database type string.
     */
    private String mapJavaTypeToDbType(String javaType) {
        return generatorConfig.getBaseConfigs().getTypeMapping().getOrDefault(javaType, "VARCHAR(255)");
    }

    /**
     * Saves a list of SQL statements to a single file within the project's resources directory.
     * The file is always overwritten.
     *
     * @param sqlStatements The list of SQL statements to save.
     * @throws IOException If an I/O error occurs while writing the file.
     */
    private void saveSqlScript(List<String> sqlStatements) throws IOException {
        if (sqlStatements.isEmpty()) {
            printInfoLog("No database schema changes detected, no SQL script saved. 🤷‍♂️");
            return;
        }

        File sqlDir = new File(project.getBasedir(), Constants.FileConstant.SQL_SCRIPT_PATH_DIRECTORY);
        if (!sqlDir.exists()) {
            printInfoLog("Creating SQL script directory: " + sqlDir.getAbsolutePath());
            sqlDir.mkdirs();
        }
        File sqlFile = new File(sqlDir, Constants.FileConstant.SQL_SCRIPT_FILE_NAME);

        // false indicates that the file should be overwritten (not appended)
        try (FileWriter writer = new FileWriter(sqlFile, StandardCharsets.UTF_8, false)) {
            writer.write("-- Generated by Codegen Maven Plugin at " + new java.util.Date() + "\n\n");
            for (String sql : sqlStatements) {
                writer.write(sql + "\n\n");
            }
            printInfoLog("SQL script saved to: " + sqlFile.getAbsolutePath() + " 💾");
        }
    }


    /**
     * 控制日志级别，如果使用比info更高日志级别时，不执行方法。
     *
     * @param logInfo
     */
    private void printInfoLog(String logInfo) {
        if (log.isInfoEnabled()) {
            log.info(logInfo);
        }
    }
}

