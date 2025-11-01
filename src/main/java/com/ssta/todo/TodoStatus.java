package com.ssta.todo;

public enum TodoStatus {
  TODO("To Do"),
  IN_PROGRESS("In Progress"),
  COMPLETE("Complete");

  private final String displayLabel;

  TodoStatus(String displayLabel) {
    this.displayLabel = displayLabel;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  /**
   * Returns the next status in the cycle: TODO -> IN_PROGRESS -> COMPLETE -> TODO
   */
  public TodoStatus next() {
    return switch (this) {
      case TODO -> IN_PROGRESS;
      case IN_PROGRESS -> COMPLETE;
      case COMPLETE -> TODO;
    };
  }
}
