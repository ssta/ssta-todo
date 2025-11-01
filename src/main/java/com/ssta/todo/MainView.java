package com.ssta.todo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
  private Div emptyStateMessage;

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

    // Create title with consistent styling
    H1 title = new H1("TODO Application");
    title.getStyle()
        .set("margin-top", "0")
        .set("margin-bottom", "var(--lumo-space-l)")
        .set("color", "var(--lumo-primary-text-color)");

    // Create filter section with consistent spacing
    HorizontalLayout filterSection = createFilterSection();
    filterSection.setWidthFull();
    filterSection.getStyle()
        .set("flex-wrap", "wrap")
        .set("margin-bottom", "var(--lumo-space-m)")
        .set("padding", "var(--lumo-space-s)")
        .set("background-color", "var(--lumo-contrast-5pct)")
        .set("border-radius", "var(--lumo-border-radius-m)");

    // Create add button with consistent styling
    Button addButton = new Button("Add New TODO");
    addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addButton.addClickListener(e -> openFormForNewItem());
    addButton.getStyle().set("margin-bottom", "var(--lumo-space-m)");

    // Create form
    form = new TodoItemForm();
    form.setSaveHandler(this::saveTodoItem);
    form.setCancelHandler(this::closeForm);
    form.setVisible(false);
    form.setWidthFull();

    // Create empty state message (before grid, since grid's refreshGrid() needs it)
    emptyStateMessage = createEmptyStateMessage();
    emptyStateMessage.setVisible(false);

    // Create grid
    createGrid();

    // Add components to view
    add(title, filterSection, addButton, form, emptyStateMessage, grid);

    setSizeFull();
    setJustifyContentMode(JustifyContentMode.START);
    setAlignItems(Alignment.STRETCH);
    setPadding(true);
    setSpacing(true);
    setMaxWidth("1400px");
    getStyle().set("margin", "0 auto");
  }

  private HorizontalLayout createFilterSection() {
    H3 filterLabel = new H3("Filter by Status:");
    filterLabel.getStyle()
        .set("margin", "0")
        .set("margin-right", "var(--lumo-space-l)")
        .set("font-size", "var(--lumo-font-size-l)")
        .set("font-weight", "600");

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
    filterLayout.getStyle().set("gap", "var(--lumo-space-m)");

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

  private Div createEmptyStateMessage() {
    Div emptyState = new Div();

    Icon emptyIcon = VaadinIcon.INFO_CIRCLE.create();
    emptyIcon.setSize("48px");
    emptyIcon.getStyle()
        .set("color", "var(--lumo-contrast-50pct)")
        .set("margin-bottom", "var(--lumo-space-m)");

    Paragraph message = new Paragraph("No TODO items match the current filter.");
    message.getStyle()
        .set("color", "var(--lumo-contrast-70pct)")
        .set("font-size", "var(--lumo-font-size-l)")
        .set("margin", "0");

    Paragraph hint = new Paragraph("Try adjusting your filters or add a new TODO item.");
    hint.getStyle()
        .set("color", "var(--lumo-contrast-50pct)")
        .set("font-size", "var(--lumo-font-size-s)")
        .set("margin-top", "var(--lumo-space-s)");

    emptyState.add(emptyIcon, message, hint);
    emptyState.getStyle()
        .set("display", "flex")
        .set("flex-direction", "column")
        .set("align-items", "center")
        .set("justify-content", "center")
        .set("padding", "var(--lumo-space-xl)")
        .set("background-color", "var(--lumo-contrast-5pct)")
        .set("border-radius", "var(--lumo-border-radius-l)")
        .set("min-height", "300px")
        .set("text-align", "center");

    return emptyState;
  }

  private Grid<TodoItem> createGrid() {
    Grid<TodoItem> todoGrid = new Grid<>(TodoItem.class, false);
    todoGrid.setHeightFull();
    todoGrid.setMinHeight("400px");
    todoGrid.setWidthFull();
    todoGrid.setMultiSort(true);
    todoGrid.setColumnReorderingAllowed(true);

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

          // Make it clickable with hover effects
          statusSpan.getStyle()
              .set("cursor", "pointer")
              .set("transition", "all 0.2s ease")
              .set("user-select", "none");

          // Add hover effect using mouseenter/mouseleave
          statusSpan.getElement().addEventListener("mouseenter", e -> {
            statusSpan.getStyle()
                .set("transform", "scale(1.05)")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.2)");
          });

          statusSpan.getElement().addEventListener("mouseleave", e -> {
            statusSpan.getStyle()
                .set("transform", "scale(1)")
                .set("box-shadow", "none");
          });

      statusSpan.getElement().addEventListener("click", event -> {
        cycleItemStatus(item);
      });

      return statusSpan;
        })
        .setHeader("Status")
        .setKey("status")
        .setFlexGrow(0)
        .setWidth("120px")
        .setResizable(true)
        .setSortable(true)
        .setComparator((item1, item2) -> item1.getStatus().compareTo(item2.getStatus()));

    // Description column - flexible, takes remaining space
    todoGrid.addColumn(TodoItem::getDescription)
        .setHeader("Description")
        .setKey("description")
        .setFlexGrow(3)
        .setAutoWidth(false)
        .setResizable(true)
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
        .setWidth("90px")
        .setResizable(true)
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
        .setFlexGrow(1)
        .setWidth("160px")
        .setResizable(true)
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
        .setWidth("180px")
        .setResizable(true);

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

      // Show/hide empty state message
      if (items.isEmpty()) {
        emptyStateMessage.setVisible(true);
        grid.setVisible(false);
      } else {
        emptyStateMessage.setVisible(false);
        grid.setVisible(true);
      }
    } catch (Exception e) {
      logger.error("Failed to refresh grid", e);
      showErrorNotification("Failed to load TODO items. Please try refreshing the page.");
      grid.setItems(new ArrayList<>());
      emptyStateMessage.setVisible(true);
      grid.setVisible(false);
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
