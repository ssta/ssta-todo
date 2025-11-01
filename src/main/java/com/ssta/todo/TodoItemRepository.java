package com.ssta.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

  /**
   * Find all TodoItems by their status
   */
  List<TodoItem> findByStatus(TodoStatus status);

  /**
   * Find all TodoItems by multiple statuses
   */
  List<TodoItem> findByStatusIn(List<TodoStatus> statuses);
}
