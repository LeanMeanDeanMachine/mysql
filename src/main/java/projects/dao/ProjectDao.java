package projects.dao;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProjectDao extends DaoBase<Project> {

    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    public Project insertProject(Project project) {
        return project;
    }

    public List<Project> fetchAllProjects() {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Project> projects = new ArrayList<>();

            while (rs.next()) {
                Project project = extractProject(rs);
                int projectId = project.getProjectId();

                List<Material> materials = fetchMaterialsForProject(conn, projectId);
                List<Step> steps = fetchStepsForProject(conn, projectId);
                List<Category> categories = fetchCategoriesForProject(conn, projectId);

                project.getMaterials().addAll(materials);
                project.getSteps().addAll(steps);
                project.getCategories().addAll(categories);

                projects.add(project);
            }

            return projects;
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public Project fetchProjectById(Integer projectId) {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractProject(rs);
                }
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }

        return null;
    }


    private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
        // SQL query to fetch materials for a project
        String sql = "SELECT m.* FROM " + MATERIAL_TABLE + " m " +
                "JOIN " + MATERIAL_TABLE + " pm USING (material_id) " +
                "WHERE pm.project_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Material> materials = new LinkedList<>();
                while (rs.next()) {
                    materials.add(extract(rs, Material.class));
                }
                return materials;
            }
        }
    }

    private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
        // SQL query to fetch steps for a project
        String sql = "SELECT s.* FROM " + STEP_TABLE + " s " +
                "JOIN " + STEP_TABLE + " ps USING (step_id) " +
                "WHERE ps.project_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Step> steps = new LinkedList<>();
                while (rs.next()) {
                    steps.add(extract(rs, Step.class));
                }
                return steps;
            }
        }
    }

    private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
        // SQL query to fetch categories for a project
        String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c " +
                "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) " +
                "WHERE pc.project_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Category> categories = new LinkedList<>();
                while (rs.next()) {
                    categories.add(extract(rs, Category.class));
                }
                return categories;
            }
        }
    }

    private Project extractProject(ResultSet rs) throws SQLException {
        Project project = new Project();

        project.setProjectId(rs.getInt("project_id"));
        project.setProjectName(rs.getString("project_name"));
        project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
        project.setActualHours(rs.getBigDecimal("actual_hours"));
        project.setDifficulty(rs.getInt("difficulty"));
        project.setNotes(rs.getString("notes"));

        return project;
    }

    public boolean modifyProjectDetails(Project project) {
        String sql = "UPDATE " + PROJECT_TABLE +
                " SET project_name = ?, estimated_hours = ?, actual_hours = ?, difficulty = ?, notes = ? " +
                "WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start a transaction
            stmt.setString(1, project.getProjectName());
            stmt.setBigDecimal(2, project.getEstimatedHours());
            stmt.setBigDecimal(3, project.getActualHours());
            stmt.setInt(4, project.getDifficulty());
            stmt.setString(5, project.getNotes());
            stmt.setInt(6, project.getProjectId());

            int result = stmt.executeUpdate();

            conn.commit(); // Commit the transaction

            return result == 1;
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public boolean deleteProject(Integer projectId) {
        String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Start the transaction
            conn.setAutoCommit(false);

            // Set the project ID parameter
            stmt.setInt(1, projectId);

            // Execute the update
            int rowsAffected = stmt.executeUpdate();

            // Commit the transaction
            conn.commit();

            // Return true if one row was affected (successful deletion)
            return rowsAffected == 1;
        } catch (SQLException e) {
            // Rollback the transaction in case of exception
            throw new DbException(e);
        }
    }

}

