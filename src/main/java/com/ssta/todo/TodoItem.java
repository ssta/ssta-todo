package com.ssta.todo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_items")
public class TodoItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Description is required")
  @Size(max = 400, message = "Description must not exceed 400 characters")
  @Column(nullable = false, length = 400)
  private String description;

  @Size(max = 400, message = "Detailed notes must not exceed 400 characters")
  @Column(length = 400)
  private String detailedNotes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TodoStatus status = TodoStatus.TODO;

  @Min(value = 1, message = "Priority must be between 1 and 5")
  @Max(value = 5, message = "Priority must be between 1 and 5")
  @Column
  private Integer priority;

  @Column
  private LocalDate dueDate;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @Column(nullable = false)
  private LocalDateTime updatedDate;

  // Constructors
  public TodoItem() {
  }

  public TodoItem(String description) {
    this.description = description;
    this.status = TodoStatus.TODO;
  }

  @PrePersist
  protected void onCreate() {
    createdDate = LocalDateTime.now();
    updatedDate = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedDate = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDetailedNotes() {
    return detailedNotes;
  }

  public void setDetailedNotes(String detailedNotes) {
    this.detailedNotes = detailedNotes;
  }

  public TodoStatus getStatus() {
    return status;
  }

  public void setStatus(TodoStatus status) {
    this.status = status;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public LocalDateTime getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(LocalDateTime updatedDate) {
    this.updatedDate = updatedDate;
  }
}
