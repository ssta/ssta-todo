# TODO Application

A simple TODO list application built with Spring Boot and Vaadin, featuring task management with priorities, due dates,
and status tracking.

## Features

- Create, edit, and delete TODO items
- Set priority levels (1-5, where 1 is highest priority)
- Set due dates with visual indicators for overdue and due today items
- Three status states: TODO, In Progress, and Complete
- Click status badges to cycle through states
- Filter items by status
- Sort by multiple columns (due date, priority, status, description)
- User preferences persist across sessions
- Responsive design with resizable columns

## Prerequisites

- Java 17 or newer (JDK 21 recommended)
- Gradle (wrapper included)

## How to Run the Application

### Using Gradle Wrapper (Recommended)

1. **Clone or navigate to the project directory:**
   ```bash
   cd ssta-todo
   ```

2. **Run the application:**

   On Windows:
   ```bash
   gradlew.bat bootRun
   ```

   On macOS/Linux:
   ```bash
   ./gradlew bootRun
   ```

3. **Access the application:**
    - The application will start on `http://localhost:8080`
    - Your default browser should open automatically
    - If not, manually navigate to `http://localhost:8080`

### Building a JAR file

To build a standalone JAR file:

```bash
./gradlew build
```

The JAR file will be created in `build/libs/ssta-todo-1.0-SNAPSHOT.jar`

Run the JAR:

```bash
java -jar build/libs/ssta-todo-1.0-SNAPSHOT.jar
```

## Database Configuration

### Database File Location

The application uses H2 database in file-based mode. The database file is stored at:

```
./data/todoapp.mv.db
```

This file is located in the project root directory under the `data/` folder.

### Database Details

- **Type:** H2 Database (file-based)
- **Location:** `./data/todoapp.mv.db`
- **Connection URL:** `jdbc:h2:file:./data/todoapp`
- **Username:** `sa`
- **Password:** (empty)

The database file is automatically created on first run and persists all your TODO items and preferences.

## Basic Usage Instructions

### Adding a TODO Item

1. Click the **"Add New TODO"** button
2. Fill in the form:
    - **Description** (required): Brief summary of the task
    - **Detailed Notes** (optional): Expand for additional details
    - **Priority** (optional): Select 1-5 (1 = highest)
    - **Due Date** (optional): Set a target completion date
3. Click **"Save"** to create the item

### Editing a TODO Item

1. Click the **"Edit"** button in the Actions column for any item
2. Modify the fields as needed
3. Click **"Save"** to update the item

### Deleting a TODO Item

1. Click the **"Delete"** button in the Actions column
2. Confirm the deletion in the dialog

### Changing Status

- Click on the status badge (TODO, In Progress, or Complete) to cycle through states
- The status will automatically advance: TODO → In Progress → Complete → TODO

### Filtering Items

Use the filter checkboxes at the top to show/hide items by status:

- **Show TODO**: Display items in TODO status
- **Show In Progress**: Display items in In Progress status
- **Show Complete**: Display completed items

Filter preferences are saved and restored when you restart the application.

### Sorting Items

- Click on any column header to sort by that column
- Click again to reverse sort direction
- Click a third time to remove sorting from that column
- Multi-column sorting is supported (holds Shift while clicking additional columns)
- Default sort: Due Date (overdue first) → Priority (1 before 5)

### Visual Indicators

- **Overdue items**: Red text with warning icon
- **Due today**: Yellow text with clock icon
- **Future dates**: Normal styling
- **Status badges**: Color-coded (TODO: gray, In Progress: blue, Complete: green)

## Project Structure

```
ssta-todo/
├── src/
│   └── main/
│       ├── java/com/ssta/todo/
│       │   ├── TodoApplication.java       # Main application entry point
│       │   ├── TodoItem.java              # Entity: TODO item
│       │   ├── TodoStatus.java            # Enum: TODO/IN_PROGRESS/COMPLETE
│       │   ├── TodoItemRepository.java    # Data access layer
│       │   ├── TodoItemService.java       # Business logic layer
│       │   ├── UserPreferences.java       # Entity: User preferences
│       │   ├── UserPreferencesRepository.java
│       │   ├── UserPreferencesService.java
│       │   ├── TodoItemForm.java          # Form component for add/edit
│       │   └── MainView.java              # Main UI view
│       └── resources/
│           └── application.properties     # Application configuration
├── data/                                  # Database files (created at runtime)
├── build.gradle                           # Gradle build configuration
├── gradle.properties                      # Gradle settings
└── README.md                              # This file
```

## Configuration

All configuration is in `src/main/resources/application.properties`:

- **Database path**: Change `spring.datasource.url` to use a different location
- **Auto-browser launch**: Set `vaadin.launch-browser=false` to disable
- **SQL logging**: Set `spring.jpa.show-sql=true` to see SQL statements

## Technology Stack

- **Backend**: Spring Boot 3.3.5
- **Frontend**: Vaadin 24.5.4
- **Database**: H2 (file-based)
- **Build Tool**: Gradle 8.10.2
- **Java Version**: 21

## Troubleshooting

### Port 8080 Already in Use

If port 8080 is already in use, add this to `application.properties`:

```properties
server.port=8081
```

### Database File Locked

If you see database lock errors, ensure no other instance of the application is running.

### Java Version Issues

Ensure you're using Java 17 or newer:

```bash
java -version
```

If needed, set JAVA_HOME to point to JDK 17+, or update `gradle.properties`:

```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-21
```

## Future Enhancements

Planned improvements for future versions:

### Phase 7: Database Migration
- PostgreSQL support for production deployments
- Data migration strategy from H2 to PostgreSQL

### Phase 8: User Experience Enhancements

- **Dark Mode**: Toggle button to switch between light and dark themes
    - Theme preference persisted across sessions
    - Full Vaadin Lumo theme integration

- **File Attachments**: Support for attaching files to TODO items
    - Drag-and-drop file upload in edit form
    - Multiple file attachments per item
    - Visual indicator in grid showing attachment count
    - File storage (database BLOB or filesystem)
    - File download functionality

- **Display Detailed Notes from Grid**: Quick access to detailed notes without opening edit form
    - Visual indicator (icon/badge) in grid for items with notes
    - Multiple display options: modal dialog, tooltip, expandable row, or side panel
    - Quick edit capability from notes display
    - Works with both plain text and markdown (once implemented)

- **Markdown Support**: Rich text editing for detailed notes
    - Markdown editor with live preview (using Vaadin markdown-editor-addon)
    - Syntax toolbar (bold, italic, lists, links, code blocks)
    - Rendered markdown display for viewing notes
    - Seamless migration - existing plain text notes work as-is in markdown

### Future Considerations
- Multi-user support with authentication
- Search and advanced filtering
- Categories and tags
- Recurring tasks
- Export/import functionality
- Email notifications for due dates

## License

This is an internal application for SSTA.
