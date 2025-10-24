package myapp;

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
        super.init();
        try {
            Class.forName("org.sqlite.JDBC");

            // Read DB path from environment variable
            String dbPath = System.getenv("DB_PATH");
            if (dbPath == null || dbPath.isEmpty()) {
                throw new ServletException("Environment variable DB_PATH is not set!");
            }

            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            System.out.println("Connected to SQLite successfully via ENV variable: " + dbPath);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("DB connection failed: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        try {
            String name = request.getParameter("name");
            String contact = request.getParameter("contact");
            String area = request.getParameter("area");
            String problem = request.getParameter("problem");

            String sql = "INSERT INTO reports(name, contact, area, problem) VALUES(?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, contact);
                ps.setString(3, area);
                ps.setString(4, problem);

                int rowsInserted = ps.executeUpdate();
                response.getWriter().println(rowsInserted > 0
                        ? "We have received the data, and we will try to help."
                        : "Failed to save data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}
