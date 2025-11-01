package com.ssta.todo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import java.util.function.Consumer;

public class TodoItemForm extends FormLayout {

  private final TextField description = new TextField("Description");
  private final TextArea detailedNotes = new TextArea("Detailed Notes");
  private final Button toggleDetailsButton = new Button("Show Details", VaadinIcon.ANGLE_DOWN.create());
  private final ComboBox<Integer> priority = new ComboBox<>("Priority");
  private final DatePicker dueDate = new DatePicker("Due Date");

  private final Button saveButton = new Button("Save");
  private final Button cancelButton = new Button("Cancel");

  private final Binder<TodoItem> binder = new BeanValidationBinder<>(TodoItem.class);

  private TodoItem currentItem;
  private Consumer<TodoItem> saveHandler;
  private Runnable cancelHandler;

  private boolean detailsVisible = false;

  public TodoItemForm() {
    addClassName("todo-item-form");

    configureFields();
    configureButtons();
    setupBinder();
    setupLayout();
  }

  private void configureFields() {
    // Description field
    description.setRequired(true);
    description.setMaxLength(400);
    description.setWidthFull();
    description.setPlaceholder("Enter task description...");

    // Detailed Notes field (initially hidden)
    detailedNotes.setMaxLength(400);
    detailedNotes.setWidthFull();
    detailedNotes.setVisible(false);
    detailedNotes.setPlaceholder("Enter detailed notes (optional)...");
    detailedNotes.setHeight("100px");

    // Toggle details button
    toggleDetailsButton.addClickListener(e -> toggleDetailsVisibility());

    // Priority ComboBox
    priority.setItems(1, 2, 3, 4, 5);
    priority.setPlaceholder("Select priority (optional)");
    priority.setClearButtonVisible(true);
    priority.setItemLabelGenerator(p -> "Priority " + p);

    // Due Date picker
    dueDate.setPlaceholder("Select due date (optional)");
    dueDate.setClearButtonVisible(true);
  }

  private void configureButtons() {
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickListener(e -> validateAndSave());

    cancelButton.addClickListener(e -> {
      if (cancelHandler != null) {
        cancelHandler.run();
      }
    });
  }

  private void setupBinder() {
    binder.forField(description)
        .asRequired("Description is required")
        .withValidator(desc -> desc.length() <= 400, "Description must not exceed 400 characters")
        .bind(TodoItem::getDescription, TodoItem::setDescription);

    binder.forField(detailedNotes)
        .withValidator(notes -> notes == null || notes.isEmpty() || notes.length() <= 400,
            "Detailed notes must not exceed 400 characters")
        .bind(TodoItem::getDetailedNotes, TodoItem::setDetailedNotes);

    binder.forField(priority)
        .bind(TodoItem::getPriority, TodoItem::setPriority);

    binder.forField(dueDate)
        .bind(TodoItem::getDueDate, TodoItem::setDueDate);
  }

  private void setupLayout() {
    setResponsiveSteps(
        new ResponsiveStep("0", 1),
        new ResponsiveStep("500px", 2)
    );

    // Add fields
    setColspan(description, 2);
    add(description);

    add(toggleDetailsButton);
    setColspan(toggleDetailsButton, 2);

    setColspan(detailedNotes, 2);
    add(detailedNotes);

    add(priority, dueDate);

    // Button layout
    HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
    buttonLayout.setSpacing(true);
    setColspan(buttonLayout, 2);
    add(buttonLayout);
  }

  private void toggleDetailsVisibility() {
    detailsVisible = !detailsVisible;
    detailedNotes.setVisible(detailsVisible);

    if (detailsVisible) {
      toggleDetailsButton.setText("Hide Details");
      toggleDetailsButton.setIcon(VaadinIcon.ANGLE_UP.create());
    } else {
      toggleDetailsButton.setText("Show Details");
      toggleDetailsButton.setIcon(VaadinIcon.ANGLE_DOWN.create());
    }
  }

  private void validateAndSave() {
    if (binder.validate().isOk()) {
      try {
        binder.writeBean(currentItem);
        if (saveHandler != null) {
          saveHandler.accept(currentItem);
        }
      } catch (Exception e) {
        // Validation error - binder will show error messages
      }
    }
  }

  public void setTodoItem(TodoItem item) {
    this.currentItem = item;
    binder.readBean(item);

    // Reset details visibility
    detailsVisible = false;
    detailedNotes.setVisible(false);
    toggleDetailsButton.setText("Show Details");
    toggleDetailsButton.setIcon(VaadinIcon.ANGLE_DOWN.create());

    // If there are existing detailed notes, show them
    if (item != null && item.getDetailedNotes() != null && !item.getDetailedNotes().isEmpty()) {
      toggleDetailsVisibility();
    }
  }

  public void setSaveHandler(Consumer<TodoItem> handler) {
    this.saveHandler = handler;
  }

  public void setCancelHandler(Runnable handler) {
    this.cancelHandler = handler;
  }

  public void clear() {
    setTodoItem(new TodoItem());
  }
}
