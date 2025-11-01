package com.ssta.todo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TodoItemService {

  private final TodoItemRepository repository;

  public TodoItemService(TodoItemRepository repository) {
    this.repository = repository;
  }

  /**
   * Get all TodoItems
   */
  public List<TodoItem> findAll() {
    return repository.findAll();
  }

  /**
   * Find TodoItems by one or more statuses
   */
  public List<TodoItem> findByStatus(TodoStatus... statuses) {
    if (statuses == null || statuses.length == 0) {
      return findAll();
    }
    return repository.findByStatusIn(Arrays.asList(statuses));
  }

  /**
   * Save (create or update) a TodoItem
   */
  public TodoItem save(TodoItem item) {
    if (item == null) {
      throw new IllegalArgumentException("TodoItem cannot be null");
    }

    // Validate description
    if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException("Description is required");
    }

    if (item.getDescription().length() > 400) {
      throw new IllegalArgumentException("Description must not exceed 400 characters");
    }

    // Validate detailed notes if present
    if (item.getDetailedNotes() != null && item.getDetailedNotes().length() > 400) {
      throw new IllegalArgumentException("Detailed notes must not exceed 400 characters");
    }

    // Validate priority if present
    if (item.getPriority() != null && (item.getPriority() < 1 || item.getPriority() > 5)) {
      throw new IllegalArgumentException("Priority must be between 1 and 5");
    }

    // Set default status if null
    if (item.getStatus() == null) {
      item.setStatus(TodoStatus.TODO);
    }

    return repository.save(item);
  }

  /**
   * Delete a TodoItem by ID
   */
  public void delete(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }

    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("TodoItem with ID " + id + " not found");
    }

    repository.deleteById(id);
  }

  /**
   * Cycle the status of a TodoItem to the next status
   */
  public TodoItem cycleStatus(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }

    Optional<TodoItem> optionalItem = repository.findById(id);
    if (optionalItem.isEmpty()) {
      throw new IllegalArgumentException("TodoItem with ID " + id + " not found");
    }

    TodoItem item = optionalItem.get();
    item.setStatus(item.getStatus().next());
    return repository.save(item);
  }

  /**
   * Get all TodoItems with custom sorting
   */
  public List<TodoItem> findAllSorted(Comparator<TodoItem> comparator) {
    if (comparator == null) {
      throw new IllegalArgumentException("Comparator cannot be null");
    }

    return repository.findAll().stream()
        .sorted(comparator)
        .collect(Collectors.toList());
  }

  /**
   * Find a TodoItem by ID
   */
  public Optional<TodoItem> findById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }
    return repository.findById(id);
  }
}
