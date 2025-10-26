package myapp;

import java.io.*;
import java.nio.file.*;
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

            // Get DB path from environment variable
            String dbPath = System.getenv("EMERGENCY_DB");
            if (dbPath == null || dbPath.isEmpty()) {
                throw new ServletException("Environment variable EMERGENCY_DB is not set.");
            }

            File dbFile = new File(dbPath);

            // If the DB file does not exist or is not writable, copy from resources to /tmp
            if (!dbFile.exists() || !dbFile.canWrite()) {
                File tmpFile = new File("/tmp/emergency.db");
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("emergency.db")) {
                    if (is == null) {
                        throw new ServletException("DB resource not found in project.");
                    }
                    Files.copy(is, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                dbPath = tmpFile.getAbsolutePath();
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
        response.setContentType("text/html");
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
