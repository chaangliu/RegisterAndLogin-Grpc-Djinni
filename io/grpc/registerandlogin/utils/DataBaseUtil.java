package io.grpc.registerandlogin.utils;

import java.sql.*;

public class DataBaseUtil {
    //mysql驱动包名
    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
    //数据库连接地址
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    //用户名
    private static final String USER_NAME = "root";
    //密码
    private static final String PASSWORD = "";

    public static String query(String key) {
        Connection connection = null;
        try {
            //加载mysql的驱动类
            Class.forName(DRIVER_NAME);
            //获取数据库连接
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            String sql2 = "INSERT INTO grpc VALUES (\"chang2\", \"3333\")";
            PreparedStatement prst2 = connection.prepareStatement(sql2);
            prst2.executeUpdate();
            //mysql查询语句
            String sql = "SELECT name FROM grpc";
            PreparedStatement prst = connection.prepareStatement(sql);
            //结果集
            ResultSet rs = prst.executeQuery();
            while (rs.next()) {
//                System.out.println("用户名:" + rs.getString("name"));
            }
            rs.close();
            prst.close();
            return rs.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}