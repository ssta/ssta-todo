package com.ssta.todo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {

  @Id
  private Long id = 1L; // Fixed to 1 for single-user application

  @Column(nullable = false)
  private Boolean showTodo = true;

  @Column(nullable = false)
  private Boolean showInProgress = true;

  @Column(nullable = false)
  private Boolean showComplete = true;

  // Constructors
  public UserPreferences() {
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Boolean getShowTodo() {
    return showTodo;
  }

  public void setShowTodo(Boolean showTodo) {
    this.showTodo = showTodo;
  }

  public Boolean getShowInProgress() {
    return showInProgress;
  }

  public void setShowInProgress(Boolean showInProgress) {
    this.showInProgress = showInProgress;
  }

  public Boolean getShowComplete() {
    return showComplete;
  }

  public void setShowComplete(Boolean showComplete) {
    this.showComplete = showComplete;
  }
}
