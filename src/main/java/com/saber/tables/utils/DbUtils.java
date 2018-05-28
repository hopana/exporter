package com.saber.tables.utils;

import com.saber.tables.TableEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 获取数据库连接 Created by Saber on 2018/5/28.
 */
public class DbUtils {

    /**
     * 单例
     */
    private static DbUtils single = null;

    /**
     * 数据库驱动
     */
    private static String driverName;
    /**
     * 数据库连接url
     */
    private static String url;
    /**
     * 用户名
     */
    private static String username;
    /**
     * 密码
     */
    private static String password;

    /**
     * 标识是否初始化数据库连接
     */
    private static boolean isInit = false;

    /**
     * 数据库连接
     */
    private static Connection conn = null;

    /**
     * 文件名称
     */
    public static String fileName;

    /**
     * 初始化数据库连接
     */
    private void init() {
        if (!isInit) {
            Properties properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = DbUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");
            try {
                // 使用properties对象加载输入流
                properties.load(new InputStreamReader(in,"UTF-8"));

                driverName = properties.getProperty("jdbc.driverName");
                url = properties.getProperty("jdbc.url");
                username = properties.getProperty("jdbc.username");
                password = properties.getProperty("jdbc.password");

                fileName = properties.getProperty("fileName");

                //获取连接
                getConnection();

                isInit = true;
            } catch (IOException e) {
                System.err.println("jdbc.properties文件不存在！");
            }
        }
    }

    private DbUtils() {
        //初始化数据库连接配置
        init();
    }

    /**
     * 单例
     *
     * @return DbUtils
     */
    public static DbUtils getInstance() {
        if (single == null) {
            single = new DbUtils();
        }
        return single;
    }


    /**
     * 获取数据库连接
     *
     * @return Connection
     */
    private Connection getConnection() {
        if (conn == null) {
            try {
                Class.forName(driverName);
                try {
                    conn = DriverManager.getConnection(url, username, password);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    /**
     * 执行sql
     *
     * @param sql
     * @return
     */
    public static ResultSet executeQuery(String sql) {

        ResultSet rs = null;
        try {
            if (conn == null || conn.isClosed()) {
                DbUtils db = DbUtils.getInstance();
                conn = db.getConnection();
            }
        } catch (Exception e) {
        }

        try {
            try (Statement sm = conn != null ? conn.createStatement() : null) {
                if (sm != null) {
                    rs = sm.executeQuery(sql);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;

    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库中所有表的表名，并添加到列表结构中
     *
     * @return 表名集合
     * @throws SQLException
     */
    public List<String> getTableNameList() {

        List<String> tableNameList = new ArrayList<String>();

        DatabaseMetaData dbmd = null;
        try {
            if (conn == null || conn.isClosed()) {
                DbUtils db = DbUtils.getInstance();
                conn = db.getConnection();
            }
            dbmd = conn.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (dbmd != null) {
            //访问当前用户下的所有表
            ResultSet rs = null;
            try {
                rs = dbmd.getTables(conn.getCatalog(), username, "%", new String[]{"TABLE"});

                while (rs.next()) {
                    tableNameList.add(rs.getString("TABLE_NAME"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return tableNameList;

    }

    /**
     * 获取数据表中所有列的列名，并添加到列表结构中
     *
     * @param tableName 表名
     * @return 列名集合
     * @throws SQLException
     */
    public static List<String> getColumnNameList(String tableName) {

        List<String> columnNameList = new ArrayList<String>();

        DatabaseMetaData dbmd = null;
        try {
            dbmd = conn.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (dbmd != null) {
            ResultSet rs = null;
            try {
                rs = dbmd.getColumns(conn.getCatalog(), "%", tableName, "%");
                while (rs.next()) {
                    columnNameList.add(rs.getString("COLUMN_NAME"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return columnNameList;
    }


    /**
     * 根据表名获取表结构信息
     *
     * @param tableName
     * @return
     */

    public static List<TableEntity> getStructOfTable(String tableName) {

        List<TableEntity> list = new ArrayList<TableEntity>();

        String sql = "SELECT " +
                "u.column_name AS columnName," +
                "u.data_type AS dataType," +
                "u.data_length AS dataLength," +
                "u.data_precision AS dataPrecision," +
                "u.data_Scale AS dataScale," +
                "u.nullable AS nullable," +
                "u.data_default AS dataDefault," +
                "c.comments AS comments," +
                "(SELECT COMMENTS FROM user_tab_comments WHERE TABLE_NAME = '" + tableName + "') AS tableComment" +
                " FROM user_tab_columns u,user_col_comments c" +
                " WHERE u.table_name='" + tableName + "' and u.table_name=c.table_name and c.column_name=u.column_name";

        try {
            if (conn == null || conn.isClosed()) {
                DbUtils db = DbUtils.getInstance();
                conn = db.getConnection();
            }
        } catch (Exception e) {
        }

        Statement statement = null;
        ResultSet rs = null;

        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);

            while (rs.next()) {
                TableEntity entity = new TableEntity();
                entity.setColumnName(rs.getString("columnName"));
                entity.setDataType(rs.getString("dataType"));
                entity.setDataLength(rs.getString("dataLength"));
                entity.setDataScale(rs.getString("dataScale"));
                entity.setNullable(rs.getString("nullable"));
                entity.setDataDefault(rs.getString("dataDefault"));
                entity.setComments(rs.getString("comments"));
                entity.setTableComment(rs.getString("tableComment"));

                list.add(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
            }

            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
            }
        }

        return list;
    }


    public static void main(String s[]) throws SQLException {

        DbUtils dbConn = DbUtils.getInstance();
        Connection conn = dbConn.getConnection();

        if (conn == null)
            System.out.println("连接失败");
        else
            System.out.println("连接成功");

        try {

            List<String> tableList = dbConn.getTableNameList();//取出当前用户的所有表

            //List tableList = dbConn.getColumnNameList(conn, "LOGIN");//表名称必须是大写的，取出当前表的所有列

            System.out.println(tableList.size());

            for (String tableName : tableList) {
                System.out.println(tableName);

                getColumnNameList(tableName);

            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
