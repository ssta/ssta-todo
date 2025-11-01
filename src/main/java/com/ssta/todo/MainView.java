package com.ssta.todo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

  private static final Logger logger = LoggerFactory.getLogger(MainView.class);

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
    try {
      currentPreferences = preferencesService.getPreferences();
    } catch (Exception e) {
      logger.error("Failed to load user preferences", e);
      showErrorNotification("Failed to load user preferences. Using defaults.");
      // Create default preferences as fallback
      currentPreferences = new UserPreferences();
      currentPreferences.setShowTodo(true);
      currentPreferences.setShowInProgress(true);
      currentPreferences.setShowComplete(true);
    }

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
    try {
      preferencesService.updatePreferences(currentPreferences);
    } catch (Exception e) {
      logger.error("Failed to update user preferences", e);
      showErrorNotification("Failed to save filter preferences. Your settings may not be preserved.");
    }
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
    try {
      // Apply filtering based on checkbox states
      List<TodoStatus> statusesToShow = new ArrayList<>();

      if (currentPreferences.getShowTodo()) {
        statusesToShow.add(TodoStatus.TODO);
      }
      if (currentPreferences.getShowInProgress()) {
        statusesToShow.add(TodoStatus.IN_PROGRESS);
      }
      if (currentPreferences.getShowComplete()) {
        statusesToShow.add(TodoStatus.COMPLETE);
      }

      // Get filtered items from service
      List<TodoItem> items;
      if (statusesToShow.isEmpty()) {
        // If no statuses selected, show empty list
        items = new ArrayList<>();
      } else if (statusesToShow.size() == 3) {
        // If all statuses selected, get all items (optimization)
        items = todoItemService.findAll();
      } else {
        // Get items with selected statuses
        items = todoItemService.findByStatus(statusesToShow.toArray(new TodoStatus[0]));
      }

      grid.setItems(items);
    } catch (Exception e) {
      logger.error("Failed to refresh grid", e);
      showErrorNotification("Failed to load TODO items. Please try refreshing the page.");
      grid.setItems(new ArrayList<>());
    }
  }

  private void saveTodoItem(TodoItem item) {
    try {
      todoItemService.save(item);
      refreshGrid();
      closeForm();
      showSuccessNotification("TODO item saved successfully.");
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while saving TODO item", e);
      showErrorNotification("Validation error: " + e.getMessage());
    } catch (Exception e) {
      logger.error("Failed to save TODO item", e);
      showErrorNotification("Failed to save TODO item. Please try again.");
    }
  }

  private void editTodoItem(TodoItem item) {
    form.setTodoItem(item);
    form.setVisible(true);
  }

  private void deleteTodoItem(TodoItem item) {
    ConfirmDialog dialog = new ConfirmDialog();
    dialog.setHeader("Delete TODO Item");
    dialog.setText("Are you sure you want to delete this TODO item: \"" + item.getDescription() + "\"?");

    dialog.setCancelable(true);
    dialog.setCancelText("Cancel");

    dialog.setConfirmText("Delete");
    dialog.setConfirmButtonTheme("error primary");

    dialog.addConfirmListener(event -> {
      try {
        todoItemService.delete(item.getId());
        refreshGrid();
        showSuccessNotification("TODO item deleted successfully.");
      } catch (IllegalArgumentException e) {
        logger.warn("Validation error while deleting TODO item", e);
        showErrorNotification("Error: " + e.getMessage());
      } catch (Exception e) {
        logger.error("Failed to delete TODO item", e);
        showErrorNotification("Failed to delete TODO item. Please try again.");
      }
    });

    dialog.open();
  }

  private void cycleItemStatus(TodoItem item) {
    try {
      todoItemService.cycleStatus(item.getId());
      refreshGrid();
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while cycling status", e);
      showErrorNotification("Error: " + e.getMessage());
    } catch (Exception e) {
      logger.error("Failed to update TODO item status", e);
      showErrorNotification("Failed to update status. Please try again.");
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

  /**
   * Show an error notification to the user
   */
  private void showErrorNotification(String message) {
    Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
  }

  /**
   * Show a success notification to the user
   */
  private void showSuccessNotification(String message) {
    Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }
}
