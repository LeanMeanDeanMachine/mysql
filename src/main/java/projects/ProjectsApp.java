package projects;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
  private Scanner scanner = new Scanner(System.in);
  private ProjectService projectService = new ProjectService();
  private Project curProject = null;

  // @formatter:off
  private List<String> operations = List.of(
          "1) Add a project",
          "2) List projects",
          "3) Select a project",
          "4) Update project details",
          "5) Delete a project"
  );
  // @formatter:on

  public static void main(String[] args) {
    new ProjectsApp().processUserSelections();
  }

  private void processUserSelections() {
    boolean done = false;

    while (!done) {
      try {
        int selection = getUserSelection();

        switch (selection) {
          case -1:
            done = exitMenu();
            break;

          case 1:
            createProject();
            break;

          case 2:
            listProjects();
            break;

          case 3:
            selectProject();
            break;

          case 4:
            updateProjectDetails();
            break;

          case 5:
            deleteProject();
            break;

          default:
            System.out.println("\n" + selection + " is not a valid selection. Try again.");
            break;
        }
      } catch (Exception e) {
        System.out.println("\nError: " + e + " Try again.");
      }
    }
  }

  private void selectProject() throws SQLException {
    List<Project> projects = projectService.fetchAllProjects();
    System.out.println("\nAvailable Projects:");
    for (Project project : projects) {
      System.out.println("  " + project.getProjectId() + ": " + project.getProjectName());
    }
    Integer projectId = getIntInput("Enter the ID of the project you want to select");

    Project selectedProject = projects.stream()
            .filter(project -> project.getProjectId().equals(projectId))
            .findFirst()
            .orElse(null);

    if (selectedProject == null) {
      System.out.println("Invalid project ID. Please try again.");
    } else {
      curProject = selectedProject;
      System.out.println("You have selected project: " + curProject);
    }
  }

  private void createProject() {
    String projectName = getStringInput("Enter the project name");
    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
    BigDecimal actualHours = getDecimalInput("Enter the actual hours");
    Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
    String notes = getStringInput("Enter the project notes");

    Project project = new Project();

    project.setProjectName(projectName);
    project.setEstimatedHours(estimatedHours);
    project.setActualHours(actualHours);
    project.setDifficulty(difficulty);
    project.setNotes(notes);

    Project dbProject = projectService.addProject(project);
    System.out.println("You have successfully created project: " + dbProject);
  }

  private void listProjects() throws SQLException {
    List<Project> projects = projectService.fetchAllProjects();
    System.out.println("\nProjects:");

    for (Project project : projects) {
      System.out.println("  " + project.getProjectId() + ": " + project.getProjectName());
    }
  }

  private void updateProjectDetails() {
    if (curProject == null) {
      System.out.println("\nPlease select a project.");
      return;
    }

    System.out.println("\nCurrent project details:");
    System.out.println("Project ID: " + curProject.getProjectId());
    System.out.println("Project Name: " + curProject.getProjectName());
    System.out.println("Estimated Hours: " + curProject.getEstimatedHours());
    System.out.println("Actual Hours: " + curProject.getActualHours());
    System.out.println("Difficulty: " + curProject.getDifficulty());
    System.out.println("Notes: " + curProject.getNotes());

    String projectName = getStringInput("\nEnter the new project name (or leave blank to keep current value)");
    BigDecimal estimatedHours = getDecimalInput("Enter the new estimated hours (or leave blank to keep current value)");
    BigDecimal actualHours = getDecimalInput("Enter the new actual hours (or leave blank to keep current value)");
    Integer difficulty = getIntInput("Enter the new project difficulty (1-5, or leave blank to keep current value)");
    String notes = getStringInput("Enter the new project notes (or leave blank to keep current value)");

    Project updatedProject = new Project();
    updatedProject.setProjectId(curProject.getProjectId());
    updatedProject.setProjectName(Objects.requireNonNullElse(projectName, curProject.getProjectName()));
    updatedProject.setEstimatedHours(Objects.requireNonNullElse(estimatedHours, curProject.getEstimatedHours()));
    updatedProject.setActualHours(Objects.requireNonNullElse(actualHours, curProject.getActualHours()));
    updatedProject.setDifficulty(Objects.requireNonNullElse(difficulty, curProject.getDifficulty()));
    updatedProject.setNotes(Objects.requireNonNullElse(notes, curProject.getNotes()));

    projectService.modifyProjectDetails(updatedProject);

    // Reread the current project to pick up the changes
    curProject = projectService.fetchProjectById(curProject.getProjectId());

    System.out.println("\nProject details updated successfully:");
    System.out.println("Project ID: " + curProject.getProjectId());
    System.out.println("Project Name: " + curProject.getProjectName());
    System.out.println("Estimated Hours: " + curProject.getEstimatedHours());
    System.out.println("Actual Hours: " + curProject.getActualHours());
    System.out.println("Difficulty: " + curProject.getDifficulty());
    System.out.println("Notes: " + curProject.getNotes());
  }

  private void deleteProject() throws SQLException {
    listProjects();
    Integer projectId = getIntInput("\nEnter the ID of the project you want to delete");

    projectService.deleteProject(projectId);

    if (curProject != null && curProject.getProjectId().equals(projectId)) {
      curProject = null;
    }

    System.out.println("\nProject with ID=" + projectId + " has been deleted.");
  }

  private int getUserSelection() {
    System.out.println("\nSelect an operation (enter the number):");

    for (String operation : operations) {
      System.out.println(operation);
    }

    System.out.print("Selection: ");
    String input = scanner.nextLine();
    return Integer.parseInt(input.trim());
  }

  private boolean exitMenu() {
    System.out.println("\nExiting the application...");
    scanner.close();
    return true;
  }

  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");
    return scanner.nextLine().trim();
  }

  private Integer getIntInput(String prompt) {
    while (true) {
      try {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine().trim();
        return Integer.parseInt(input);
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a valid integer.");
      }
    }
  }

  private BigDecimal getDecimalInput(String prompt) {
    while (true) {
      try {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine().trim();
        return new BigDecimal(input);
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a valid decimal number.");
      }
    }
  }
}
