package myapp;

import java.io.File;
import java.io.IOException;
import java.sql.*;
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

            // Get DB relative path from environment variable
            String relativePath = System.getenv("EMERGENCY_DB"); // "WEB-INF/emergency.db"
            if (relativePath == null || relativePath.isEmpty()) {
                throw new ServletException("Environment variable EMERGENCY_DB is not set.");
            }

            // Convert relative path to absolute path on the server
            String dbPath = getServletContext().getRealPath(relativePath);
            if (dbPath == null) {
                throw new ServletException("Cannot resolve DB path: " + relativePath);
            }

            File dbFile = new File(dbPath);
            
            // Optional: check if file exists
            if (!dbFile.exists()) {
                throw new ServletException("Database file not found at: " + dbPath);
            }

            // Connect to SQLite
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to SQLite successfully! Path: " + dbPath);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("DB connection failed: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        try {
            String name = request.getParameter("name");
            String contact = request.getParameter("contact");
            String area = request.getParameter("area");
            String problem = request.getParameter("problem");

            String sql = "INSERT INTO reports(name, contact, area, problem) VALUES(?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, contact);
            ps.setString(3, area);
            ps.setString(4, problem);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                response.getWriter().println("<h2>We have received the data and we will try to help.</h2>");
            } else {
                response.getWriter().println("<h2>Failed to save data.</h2>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("<h2>Error: " + e.getMessage() + "</h2>");
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
