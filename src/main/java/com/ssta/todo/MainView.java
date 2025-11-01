package com.ssta.todo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Route("")
public class MainView extends VerticalLayout {

  private final UserPreferencesService preferencesService;
  private final TodoItemService todoItemService;

  private Checkbox showTodoCheckbox;
  private Checkbox showInProgressCheckbox;
  private Checkbox showCompleteCheckbox;

  private UserPreferences currentPreferences;

  private TodoItemForm form;
  private Grid<TodoItem> grid;

  public MainView(UserPreferencesService preferencesService, TodoItemService todoItemService) {
    this.preferencesService = preferencesService;
    this.todoItemService = todoItemService;

    // Load current preferences
    currentPreferences = preferencesService.getPreferences();

    // Create title
    H1 title = new H1("TODO Application");

    // Create filter section
    HorizontalLayout filterSection = createFilterSection();

    // Create add button
    Button addButton = new Button("Add New TODO");
    addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addButton.addClickListener(e -> openFormForNewItem());

    // Create form
    form = new TodoItemForm();
    form.setSaveHandler(this::saveTodoItem);
    form.setCancelHandler(this::closeForm);
    form.setVisible(false);

    // Create grid
    createGrid();

    // Add components to view
    add(title, filterSection, addButton, form, grid);

    setSizeFull();
    setJustifyContentMode(JustifyContentMode.START);
    setAlignItems(Alignment.START);
    setPadding(true);
    setSpacing(true);
  }

  private HorizontalLayout createFilterSection() {
    H3 filterLabel = new H3("Filter by Status:");
    filterLabel.getStyle().set("margin-right", "20px");

    // Create checkboxes bound to preferences
    showTodoCheckbox = new Checkbox("Show TODO");
    showTodoCheckbox.setValue(currentPreferences.getShowTodo());
    showTodoCheckbox.addValueChangeListener(event -> {
      currentPreferences.setShowTodo(event.getValue());
      updatePreferences();
      refreshGrid();
    });

    showInProgressCheckbox = new Checkbox("Show In Progress");
    showInProgressCheckbox.setValue(currentPreferences.getShowInProgress());
    showInProgressCheckbox.addValueChangeListener(event -> {
      currentPreferences.setShowInProgress(event.getValue());
      updatePreferences();
      refreshGrid();
    });

    showCompleteCheckbox = new Checkbox("Show Complete");
    showCompleteCheckbox.setValue(currentPreferences.getShowComplete());
    showCompleteCheckbox.addValueChangeListener(event -> {
      currentPreferences.setShowComplete(event.getValue());
      updatePreferences();
      refreshGrid();
    });

    HorizontalLayout filterLayout = new HorizontalLayout(
        filterLabel,
        showTodoCheckbox,
        showInProgressCheckbox,
        showCompleteCheckbox
    );
    filterLayout.setAlignItems(Alignment.CENTER);
    filterLayout.setSpacing(true);

    return filterLayout;
  }

  private void updatePreferences() {
    preferencesService.updatePreferences(currentPreferences);
  }

  private Grid<TodoItem> createGrid() {
    Grid<TodoItem> todoGrid = new Grid<>(TodoItem.class, false);
    todoGrid.setHeight("600px");
    todoGrid.setWidthFull();
    todoGrid.setMultiSort(true);

    // Status column - custom component with clickable indicator
    Grid.Column<TodoItem> statusColumn = todoGrid.addComponentColumn(item -> {
      Span statusSpan = new Span(item.getStatus().getDisplayLabel());
      statusSpan.getElement().getThemeList().add("badge");

      // Style based on status
      switch (item.getStatus()) {
        case TODO -> statusSpan.getElement().getThemeList().add("badge");
        case IN_PROGRESS -> statusSpan.getElement().getThemeList().add("badge primary");
        case COMPLETE -> statusSpan.getElement().getThemeList().add("badge success");
      }

      // Make it clickable and add click handler to cycle status
      statusSpan.getStyle().set("cursor", "pointer");
      statusSpan.getElement().addEventListener("click", event -> {
        cycleItemStatus(item);
      });

      return statusSpan;
        })
        .setHeader("Status")
        .setKey("status")
        .setFlexGrow(0)
        .setWidth("150px")
        .setSortable(true)
        .setComparator((item1, item2) -> item1.getStatus().compareTo(item2.getStatus()));

    // Description column
    todoGrid.addColumn(TodoItem::getDescription)
        .setHeader("Description")
        .setKey("description")
        .setFlexGrow(1)
        .setSortable(true)
        .setComparator((item1, item2) -> {
          String desc1 = item1.getDescription() != null ? item1.getDescription() : "";
          String desc2 = item2.getDescription() != null ? item2.getDescription() : "";
          return desc1.compareToIgnoreCase(desc2);
        });

    // Priority column - show empty for null
    todoGrid.addColumn(item -> item.getPriority() != null ? item.getPriority().toString() : "")
        .setHeader("Priority")
        .setKey("priority")
        .setFlexGrow(0)
        .setWidth("100px")
        .setSortable(true)
        .setComparator((item1, item2) -> {
          // Nulls last, then sort by priority (1 = highest, 5 = lowest)
          if (item1.getPriority() == null && item2.getPriority() == null) {return 0;}
          if (item1.getPriority() == null) {return 1;}
          if (item2.getPriority() == null) {return -1;}
          return Integer.compare(item1.getPriority(), item2.getPriority());
        });

    // Due Date column - show empty for null, with visual indicators
    todoGrid.addComponentColumn(item -> {
          if (item.getDueDate() == null) {
            return new Span("");
          }

          LocalDate today = LocalDate.now();
          LocalDate dueDate = item.getDueDate();
          String formattedDate = dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

          HorizontalLayout dateLayout = new HorizontalLayout();
          dateLayout.setSpacing(true);
          dateLayout.setAlignItems(Alignment.CENTER);

          Span dateSpan = new Span(formattedDate);

          // Add visual indicators based on due date
          if (dueDate.isBefore(today)) {
            // Overdue - red warning style
            dateSpan.getStyle().set("color", "var(--lumo-error-text-color)");
            dateSpan.getStyle().set("font-weight", "bold");
            Icon warningIcon = VaadinIcon.WARNING.create();
            warningIcon.setColor("var(--lumo-error-color)");
            warningIcon.setSize("16px");
            dateLayout.add(warningIcon, dateSpan);
          } else if (dueDate.isEqual(today)) {
            // Due today - yellow/warning style
            dateSpan.getStyle().set("color", "var(--lumo-warning-text-color)");
            dateSpan.getStyle().set("font-weight", "bold");
            Icon clockIcon = VaadinIcon.CLOCK.create();
            clockIcon.setColor("var(--lumo-warning-color)");
            clockIcon.setSize("16px");
            dateLayout.add(clockIcon, dateSpan);
          } else {
            // Future date - normal style
            dateLayout.add(dateSpan);
          }

          return dateLayout;
        })
        .setHeader("Due Date")
        .setKey("dueDate")
        .setFlexGrow(0)
        .setWidth("180px")
        .setSortable(true)
        .setComparator((item1, item2) -> {
          // Custom comparator: overdue dates first, then by date, nulls last
          LocalDate today = LocalDate.now();
          LocalDate date1 = item1.getDueDate();
          LocalDate date2 = item2.getDueDate();

          // Handle nulls - nulls last
          if (date1 == null && date2 == null) {return 0;}
          if (date1 == null) {return 1;}
          if (date2 == null) {return -1;}

          // Check if overdue
          boolean overdue1 = date1.isBefore(today);
          boolean overdue2 = date2.isBefore(today);

          // Both overdue or both not overdue - sort by date
          if (overdue1 == overdue2) {
            return date1.compareTo(date2);
          }

          // One is overdue, one is not - overdue comes first
          return overdue1 ? -1 : 1;
        });

    // Actions column with Edit and Delete buttons
    todoGrid.addComponentColumn(item -> {
          Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
          editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
          editButton.addClickListener(e -> editTodoItem(item));

          Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
          deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
          deleteButton.addClickListener(e -> deleteTodoItem(item));

          HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
          actions.setSpacing(true);
          return actions;
        })
        .setHeader("Actions")
        .setKey("actions")
        .setFlexGrow(0)
        .setWidth("200px");

    // Set default sort order: Due Date (ascending, overdue first) then Priority (ascending)
    Grid.Column<TodoItem> dueDateColumn = todoGrid.getColumnByKey("dueDate");
    Grid.Column<TodoItem> priorityColumn = todoGrid.getColumnByKey("priority");

    todoGrid.sort(java.util.List.of(
        new com.vaadin.flow.component.grid.GridSortOrder<>(dueDateColumn, com.vaadin.flow.data.provider.SortDirection.ASCENDING),
        new com.vaadin.flow.component.grid.GridSortOrder<>(priorityColumn, com.vaadin.flow.data.provider.SortDirection.ASCENDING)
    ));

    this.grid = todoGrid;

    // Load initial data
    refreshGrid();

    return todoGrid;
  }

  private void refreshGrid() {
    // TODO: Apply filtering based on checkbox states in step 19
    // For now, just load all items
    grid.setItems(todoItemService.findAll());
  }

  private void saveTodoItem(TodoItem item) {
    try {
      todoItemService.save(item);
      refreshGrid();
      closeForm();
    } catch (Exception e) {
      // TODO: Show error notification to user (will be implemented in step 20)
      e.printStackTrace();
    }
  }

  private void editTodoItem(TodoItem item) {
    // TODO: This will be implemented in step 17
  }

  private void deleteTodoItem(TodoItem item) {
    // TODO: This will be implemented in step 18
  }

  private void cycleItemStatus(TodoItem item) {
    try {
      todoItemService.cycleStatus(item.getId());
      refreshGrid();
    } catch (Exception e) {
      // TODO: Show error notification to user (will be implemented in step 20)
      e.printStackTrace();
    }
  }

  private void closeForm() {
    form.setVisible(false);
    form.clear();
  }

  private void openFormForNewItem() {
    form.setTodoItem(new TodoItem());
    form.setVisible(true);
  }
}
