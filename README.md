# InterviewAI

Project scaffold for InterviewAI — JavaFX app with course map, AI prompts, simulation and admin panels.

Project structure (generated):
- src/main/java/... : Java source skeletons
- src/main/resources/fxml : FXML views
- src/main/resources/css : Stylesheets
- src/main/resources/images : Images and badges
- src/main/resources/db/schema.sql : DB schema script
- src/main/resources/config/config.properties : Example config
- lib/ : external jars (place here if not using Maven)
- pom.xml : Maven project file (basic)

How to run (quick):
1. Install JDK 11+ and JavaFX runtime or use OpenJFX.
2. Build and run with Maven (recommended):

	Install JDK 11+ and run (Windows PowerShell example):

```powershell
mvn clean package
mvn javafx:run
```

The project includes the `javafx-maven-plugin` and JavaFX dependencies in `pom.xml`. If you get errors related to JavaFX native libraries, ensure you have a matching JavaFX SDK or set the correct platform classifier in the dependencies.
3. Or run from your IDE: import as Maven project and run `com.interviewai.main.MainApp`.
3. Configure DB credentials in `src/main/resources/config/config.properties` and run `schema.sql` in your MySQL server.

Notes:
- This is a scaffold with skeleton classes. Implement controllers and services before running.
- Do NOT commit real API keys to `config.properties`.
