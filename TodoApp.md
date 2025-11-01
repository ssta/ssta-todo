# TODO Application - MVP Requirements

## Technology Stack

- **Backend Framework**: Spring
- **Frontend Framework**: Vaadin
- **Database**: SQLite or H2 (initial MVP)
  - Future: PostgreSQL (post-MVP)

## Core Features

### 1. Add TODO Items

Users can create new TODO items with the following optional attributes:
- **Priority** (optional)
- **Due Date** (optional)

Both attributes may be set independently or together.

### 2. Display TODO Items

The application displays TODO items with configurable sorting:
- Sort by **Due Date**
- Sort by **Priority**
- Sort by combination of **Due Date and Priority**

Sorting preference is user-configurable.

### 3. Edit TODO Items

Users can edit existing TODO items to update:
- Task description
- Priority
- Due Date

### 4. Delete TODO Items

Users can delete TODO items from the list.

### 5. Mark Items as Done

Users can mark TODO items as completed/done.

### 6. Data Persistence

TODO items are persisted to a relational database:
- **MVP**: SQLite or H2
- **Post-MVP**: PostgreSQL

## MVP Scope

This represents the Minimum Viable Product. Additional features (filtering, search, categories, tags, etc.) are out of scope for the initial MVP release.

---

## Detailed Requirements

### Status Management
- TODO items have three states: **TODO**, **In Progress**, and **Complete**
- Status can be cycled by clicking a status indicator in a dedicated status column
- Status cycle: TODO → In Progress → Complete → TODO (loops)

### Priority System
- Priority levels: 1-5 (1 = highest priority, 5 = lowest priority)
- Priority is optional for TODO items

### Task Description
- Single-line summary field (primary display)
- Optional multi-line expansion for detailed notes
- Character limit: 400 characters (may expand post-MVP)

### Filtering
- Checkboxes/toggles to filter by status: "Show TODO", "Show In Progress", "Show Complete"
- Default: All three statuses shown
- Users can view any combination of statuses
- Filter state persists across application restarts

### Sorting
- Sortable by clicking column headers
- Clicking same header cycles: Ascending → Descending → Not Used
- Clicking different headers adds secondary/tertiary sorts
- Default sort order: Due Date (ascending, overdue first) → Priority (ascending, 1 before 5)
- Sort order: Overdue items first, then by date, with priority as secondary sort

### Due Date Handling
- Date-only (no time component for MVP)
- Visual indicators:
  - Overdue items (due date < today)
  - Due today items (due date = today)

### User Interface
- Single view with dynamic filtering (not separate views/tabs)
- Grid/table layout with sortable columns
- Columns: Status (clickable), Description, Priority, Due Date, Actions (Edit/Delete)

---

## Implementation Steps

### Phase 1: Project Setup and Database Layer

1. **Initialize Spring Boot Project**
   - [x] Create Spring Boot application with Vaadin dependency
   - [x] Add H2 database dependency (file-based mode)
   - [x] Configure application.properties for H2 file persistence
   - [x] Set up basic project structure (flat package layout for simplicity)

2. **Create Domain Model**
    - [x] Create `TodoItem` entity class with fields:
        - [x] `id` (Long, auto-generated)
        - [x] `description` (String, max 400 chars, required)
        - [x] `detailedNotes` (String, optional, text type)
        - [x] `status` (Enum: TODO, IN_PROGRESS, COMPLETE)
        - [x] `priority` (Integer, 1-5, optional)
        - [x] `dueDate` (LocalDate, optional)
        - [x] `createdDate` (LocalDateTime, auto-set)
        - [x] `updatedDate` (LocalDateTime, auto-update)
    - [x] Add JPA annotations (@Entity, @Id, @GeneratedValue, etc.)
    - [x] Add validation annotations (@Size, @Min, @Max)

3. **Create Status Enum**
    - [x] Create `TodoStatus` enum with values: TODO, IN_PROGRESS, COMPLETE
    - [x] Add `next()` method to cycle through statuses
    - [x] Add display labels for UI

4. **Create Repository Layer**
    - [x] Create `TodoItemRepository` interface extending `JpaRepository<TodoItem, Long>`
    - [x] Add custom query methods if needed for filtering

5. **Create Service Layer**
    - [x] Create `TodoItemService` class with methods:
        - [x] `findAll()` - get all items
        - [x] `findByStatus(TodoStatus...)` - filter by one or more statuses
        - [x] `save(TodoItem)` - create/update item
        - [x] `delete(Long id)` - delete item
        - [x] `cycleStatus(Long id)` - advance item to next status
        - [x] `findAllSorted(Comparator)` - get all with custom sort
    - [x] Add business logic and validation

### Phase 2: User Preferences Persistence

6. **Create User Preferences Model**
    - [x] Create `UserPreferences` entity with fields:
        - [x] `id` (Long, fixed to 1 for single-user app)
        - [x] `showTodo` (Boolean, default true)
        - [x] `showInProgress` (Boolean, default true)
        - [x] `showComplete` (Boolean, default true)
    - [x] Add JPA annotations

7. **Create Preferences Repository and Service**
    - [x] Create `UserPreferencesRepository` extending `JpaRepository`
    - [x] Create `UserPreferencesService` with methods:
        - [x] `getPreferences()` - get or create default preferences
        - [x] `updatePreferences(UserPreferences)` - save preferences
    - [x] Initialize default preferences on first run

### Phase 3: Vaadin UI - Basic Layout

8. **Create Main View**
    - [x] Create `MainView` class annotated with `@Route("")`
    - [x] Set up basic Vaadin `VerticalLayout`
    - [x] Add application title/header

9. **Create Filter Component**
    - [x] Create filter section with three checkboxes:
        - [x] "Show TODO"
        - [x] "Show In Progress"
        - [x] "Show Complete"
    - [x] Bind checkboxes to UserPreferences
    - [x] Add value change listeners to:
        - [x] Update filter state in service
        - [x] Refresh grid data
        - [x] Persist preferences

10. **Create Add/Edit Form Component**
    - [x] Create form with fields:
        - [x] Description (TextField, maxLength 400, required)
        - [x] Detailed Notes (TextArea, maxLength 400, initially hidden/collapsed)
        - [x] Expand/collapse button for detailed notes
        - [x] Priority (ComboBox with values 1-5, optional, clearable)
        - [x] Due Date (DatePicker, optional, clearable)
    - [x] Add Save and Cancel buttons
    - [x] Add form validation
    - [x] Wire up to service layer

### Phase 4: Vaadin UI - Grid and Data Display

11. **Create TODO Items Grid**
    - [x] Create `Grid<TodoItem>` with columns:
        - [x] Status (custom component column with clickable indicator)
        - [x] Description (text column)
        - [x] Priority (number column, show empty for null)
        - [x] Due Date (date column, show empty for null)
        - [x] Actions (button column with Edit and Delete)
    - [x] Configure grid styling and responsiveness

12. **Implement Status Column**
    - [x] Create custom component for status display (Button or Span with click handler)
    - [x] Add visual styling for each status (colors/icons)
    - [x] Implement click handler to call `cycleStatus()` service method
    - [x] Refresh grid after status change

13. **Implement Due Date Visual Indicators**
    - [x] Add cell style generator for Due Date column:
        - [x] Red/warning style for overdue items (dueDate < LocalDate.now())
        - [x] Yellow/highlight style for due today (dueDate = LocalDate.now())
        - [x] Normal style for future dates
    - [x] Consider adding icon or badge in addition to color

14. **Implement Column Sorting**
    - [x] Make columns sortable by enabling `setSortable(true)`
    - [x] Implement multi-column sort with Vaadin's `GridSortOrder`
    - [x] Track sort state (column, direction, order)
    - [x] Handle sort cycling: Ascending → Descending → Remove
    - [x] Set default sort: Due Date ascending (overdue first), then Priority ascending

15. **Implement Custom Sort Comparator**
    - [x] Create comparator that handles:
        - [x] Null values (nulls last)
        - [x] Overdue dates (sort before future dates even when ascending)
        - [x] Priority ordering (1 before 5)
    - [x] Apply to grid's data provider

### Phase 5: CRUD Operations and Data Binding

16. **Implement Add TODO Functionality**
    - [x] Wire "Add" button to open form in "create" mode
    - [x] Clear form fields
    - [x] On save, validate and call service to create new item
    - [x] Refresh grid
    - [x] Show success notification

17. **Implement Edit TODO Functionality**
    - [x] Wire "Edit" button in grid to open form in "edit" mode
    - [x] Populate form with selected item data
    - [x] On save, validate and call service to update item
    - [x] Refresh grid
    - [x] Show success notification

18. **Implement Delete TODO Functionality**
    - [x] Wire "Delete" button in grid
    - [x] Show confirmation dialog
    - [x] On confirm, call service to delete item
    - [x] Refresh grid
    - [x] Show success notification

19. **Implement Data Filtering**
    - [x] Create `ConfigurableFilterDataProvider` for grid
    - [x] Apply status filters based on checkbox states
    - [x] Refresh data when filter changes
    - [x] Ensure sorting is maintained when filtering

### Phase 6: Polish and Testing

20. **Add Error Handling**
    - [x] Add try-catch blocks in service methods
    - [x] Display user-friendly error notifications in UI
    - [x] Log errors appropriately
    - [x] Handle database connection issues gracefully

21. **Add Responsive Design**
    - [x] Ensure grid columns resize appropriately
    - [x] Test on different screen sizes
    - [ ] Consider mobile layout if needed (out of scope for MVP if not required)

22. **Add UI Polish**
    - [x] Consistent spacing and padding
    - [x] Clear visual hierarchy
    - [x] Hover effects on interactive elements
    - [x] Loading indicators if needed
    - [x] Empty state message when no items match filter

23. **Testing and Validation**
    - [ ] Test all CRUD operations
    - [ ] Test status cycling
    - [ ] Test sorting with various combinations
    - [ ] Test filtering with all combinations
    - [ ] Test preference persistence (restart app)
    - [ ] Test validation (character limits, required fields)
    - [ ] Test edge cases (empty database, all items filtered out)

24. **Documentation and Comments**
    - [x] Add inline code comments for complex logic
    - [x] Document any configuration needed
    - [x] Create simple README with:
        - [x] How to run the application
        - [x] Database file location
        - [x] Basic usage instructions

### Phase 7: Future Preparation

25. **Database Abstraction for PostgreSQL Migration**
    - [ ] Ensure JPA annotations are database-agnostic
    - [ ] Use standard SQL types
    - [ ] Document what will be needed for PostgreSQL switch:
      - [ ] Add PostgreSQL dependency
      - [ ] Update application.properties
      - [ ] Create schema if needed
      - [ ] Data migration strategy

### Phase 8: Future Enhancements (Planned)

26. **Dark Mode Support**
    - [ ] Add theme toggle component to main view (top right corner)
    - [ ] Implement theme state management:
        - [ ] Store theme preference in UserPreferences entity
        - [ ] Add `darkMode` boolean field to UserPreferences
        - [ ] Persist theme selection across sessions
    - [ ] Configure Vaadin Lumo theme variants:
        - [ ] Use `UI.getCurrent().getElement().setAttribute("theme", "dark")` for dark mode
        - [ ] Use `UI.getCurrent().getElement().setAttribute("theme", "")` for light mode
    - [ ] Create toggle button with sun/moon icons
    - [ ] Apply theme on application startup based on saved preference
    - [ ] Test all UI components in both light and dark modes
    - [ ] Ensure proper contrast and readability in dark mode

27. **File Attachments for TODO Items**
    - [ ] Extend TodoItem entity for file attachments:
        - [ ] Create `Attachment` entity with fields:
            - [ ] `id` (Long, auto-generated)
            - [ ] `fileName` (String)
            - [ ] `fileSize` (Long)
            - [ ] `contentType` (String)
            - [ ] `fileData` (byte[], or file path for file system storage)
            - [ ] `uploadedDate` (LocalDateTime)
            - [ ] `todoItemId` (Long, foreign key)
        - [ ] Add one-to-many relationship between TodoItem and Attachment
        - [ ] Create `AttachmentRepository`
        - [ ] Create `AttachmentService` with upload/download/delete methods
    - [ ] Update TodoItemForm for file handling:
        - [ ] Add drag-and-drop file upload component (Vaadin Upload)
        - [ ] Display list of current attachments with delete option
        - [ ] Support multiple file uploads
        - [ ] Add basic file size validation (e.g., max 10MB per file) to prevent accidental huge uploads
    - [ ] Update MainView grid:
        - [ ] Add attachment count indicator column (e.g., "📎 3")
        - [ ] Show icon only when attachments exist
        - [ ] Consider tooltip showing file names on hover
    - [ ] Implement file storage strategy:
        - [ ] Option 1: Store files as BLOB in database (simpler, for small files)
        - [ ] Option 2: Store files on filesystem, save path in database (for larger files)
        - [ ] Create file cleanup service for deleted TODO items
    - [ ] Add download functionality:
        - [ ] Implement endpoint for file downloads
        - [ ] Add click handler to download files from attachment list

28. **Display Detailed Notes from Grid**
    - [ ] Add visual indicator in grid for items with detailed notes:
        - [ ] Add icon/badge column (e.g., "📝" or "💬") that appears only when detailedNotes is not null/empty
        - [ ] Consider combining with description column or separate column
    - [ ] Implement display options (choose one approach):
        - [ ] Option 1: Modal Dialog - Click icon to open full dialog showing rendered detailed notes
        - [ ] Option 2: Tooltip on Hover - Show preview/full notes on hovering over icon or description
        - [ ] Option 3: Expandable Row - Click icon to expand row and show notes inline below the item
        - [ ] Option 4: Side Panel - Click icon to open side drawer with notes
    - [ ] Render detailed notes appropriately:
        - [ ] If plain text: display in simple text format
        - [ ] If markdown (after step 29): render as formatted HTML
    - [ ] Consider additional features:
        - [ ] Truncate long notes in preview/tooltip
        - [ ] Add "Quick Edit" button in dialog to jump directly to editing notes
        - [ ] Show character count or indication of note length

29. **Markdown Support for Detailed Notes**
    - [ ] Add markdown editor dependency:
        - [ ] Add Vaadin markdown-editor-addon to build.gradle
        - [ ] Verify the addon includes rendering capabilities
    - [ ] Modify TodoItemForm:
        - [ ] Replace TextArea with markdown editor component
        - [ ] The addon should provide toolbar with formatting buttons (bold, italic, lists, links, etc.)
        - [ ] Enable live preview if supported by the addon
    - [ ] Display rendered markdown:
        - [ ] Use the addon's built-in rendering for displaying notes
        - [ ] Consider modal dialog or expandable section for viewing full formatted notes
    - [ ] Migration approach:
        - [ ] Simply treat all existing `detailedNotes` as markdown text
        - [ ] Plain text will render as plain text in markdown (no special handling needed)
        - [ ] No database schema changes required
        - [ ] No need for format field - markdown handles plain text gracefully

30. **JIRA Ticket Reference Integration**
    - [ ] Extend TodoItem entity:
        - [ ] Add `jiraTicket` field (String, optional)
        - [ ] Add validation for JIRA ticket format (e.g., "PROJ-1234" pattern)
    - [ ] Add configuration in application.properties:
        - [ ] Add `jira.base.url` property (e.g., "https://jira.company.com/browse/")
        - [ ] Add `jira.ticket.pattern` property for validation (e.g., "[A-Z]+-[0-9]+")
        - [ ] Make URL pattern configurable (e.g., "${jira.base.url}${ticket}")
    - [ ] Update TodoItemForm:
        - [ ] Add JIRA ticket TextField (optional)
        - [ ] Add format validation with helpful error message
        - [ ] Show preview of full JIRA URL when ticket is entered
        - [ ] Add "Test Link" button to open JIRA in browser for verification
    - [ ] Update MainView grid:
        - [ ] Add JIRA ticket column showing ticket reference
        - [ ] Render as clickable hyperlink when ticket is present
        - [ ] Link opens in new browser tab/window
        - [ ] Show icon (e.g., 🔗 or JIRA logo) next to ticket number
        - [ ] Handle case where JIRA base URL is not configured (show plain text)
    - [ ] Configuration management:
        - [ ] Create service to read and validate JIRA configuration
        - [ ] Provide sensible defaults or graceful degradation if not configured
        - [ ] Log warning if JIRA integration is used but base URL not set
    - [ ] Additional features to consider:
        - [ ] Tooltip showing full JIRA URL on hover
        - [ ] Support for multiple ticket references (comma-separated)
        - [ ] Auto-format ticket text to uppercase
        - [ ] Quick copy ticket number to clipboard

---

## Technical Notes

### Package Structure (Flat Layout)
```
com.example.todo
├── TodoApplication.java (main)
├── TodoItem.java (entity)
├── TodoStatus.java (enum)
├── TodoItemRepository.java
├── TodoItemService.java
├── UserPreferences.java (entity)
├── UserPreferencesRepository.java
├── UserPreferencesService.java
└── MainView.java (UI)
```

### Database Configuration (H2 File-Based)
```properties
spring.datasource.url=jdbc:h2:file:./data/todoapp
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
```

### Dependencies Required
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 Database
- Vaadin Spring Boot Starter
- Validation API (included in Spring Boot)

---

## Out of Scope for MVP

- Multi-user support / authentication
- Search functionality
- Categories or tags
- Recurring tasks
- Attachments
- Time tracking
- Notifications/reminders
- Import/export functionality
- Themes/customization
- Mobile app
- API endpoints (REST)
- Advanced reporting
