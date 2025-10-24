package myapp;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/submitForm")
public class Database extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection conn;

    @Override
    public void init() throws ServletException {
        try {
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");

            // Load DB path from config.properties
            Properties props = new Properties();
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/config.properties");
            props.load(input);
            String dbPath = props.getProperty("db.path");

            // Connect to SQLite
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to SQLite successfully via config!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("DB connection failed: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Outer try to catch any exception
        try {
            String name = request.getParameter("name");
            String contact = request.getParameter("contact");
            String area = request.getParameter("area");
            String problem = request.getParameter("problem");

            // Insert data into helpline table
            String sql = "INSERT INTO reports(name, contact, area, problem) VALUES(?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, contact);
            ps.setString(3, area);
            ps.setString(4, problem);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                response.getWriter().println("We have received the data,and we will try to help. ");
            } else {
                response.getWriter().println("Failed to save data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}