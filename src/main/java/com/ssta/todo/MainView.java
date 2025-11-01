package com.ssta.todo;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

  private final UserPreferencesService preferencesService;

  private Checkbox showTodoCheckbox;
  private Checkbox showInProgressCheckbox;
  private Checkbox showCompleteCheckbox;

  private UserPreferences currentPreferences;

  public MainView(UserPreferencesService preferencesService) {
    this.preferencesService = preferencesService;

    // Load current preferences
    currentPreferences = preferencesService.getPreferences();

    // Create title
    H1 title = new H1("TODO Application");

    // Create filter section
    HorizontalLayout filterSection = createFilterSection();

    // Add components to view
    add(title, filterSection);

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

  private void refreshGrid() {
    // TODO: This will be implemented when we create the grid in step 11
    // For now, this is a placeholder
  }
}
