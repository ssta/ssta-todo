package com.ssta.todo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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

  private static final Logger logger = LoggerFactory.getLogger(TodoItemService.class);
  private final TodoItemRepository repository;

  public TodoItemService(TodoItemRepository repository) {
    this.repository = repository;
  }

  /**
   * Get all TodoItems
   */
  public List<TodoItem> findAll() {
    try {
      return repository.findAll();
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all TodoItems", e);
      throw new RuntimeException("Failed to retrieve TODO items from database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while fetching all TodoItems", e);
      throw new RuntimeException("An unexpected error occurred while retrieving TODO items", e);
    }
  }

  /**
   * Find TodoItems by one or more statuses
   */
  public List<TodoItem> findByStatus(TodoStatus... statuses) {
    try {
      if (statuses == null || statuses.length == 0) {
        return findAll();
      }
      return repository.findByStatusIn(Arrays.asList(statuses));
    } catch (DataAccessException e) {
      logger.error("Database error while filtering TodoItems by status", e);
      throw new RuntimeException("Failed to filter TODO items by status", e);
    } catch (Exception e) {
      logger.error("Unexpected error while filtering TodoItems by status", e);
      throw new RuntimeException("An unexpected error occurred while filtering TODO items", e);
    }
  }

  /**
   * Save (create or update) a TodoItem
   */
  public TodoItem save(TodoItem item) {
    try {
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

      TodoItem savedItem = repository.save(item);
      logger.info("Successfully saved TodoItem with ID: {}", savedItem.getId());
      return savedItem;
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while saving TodoItem: {}", e.getMessage());
      throw e;
    } catch (DataAccessException e) {
      logger.error("Database error while saving TodoItem", e);
      throw new RuntimeException("Failed to save TODO item to database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while saving TodoItem", e);
      throw new RuntimeException("An unexpected error occurred while saving TODO item", e);
    }
  }

  /**
   * Delete a TodoItem by ID
   */
  public void delete(Long id) {
    try {
      if (id == null) {
        throw new IllegalArgumentException("ID cannot be null");
      }

      if (!repository.existsById(id)) {
        throw new IllegalArgumentException("TodoItem with ID " + id + " not found");
      }

      repository.deleteById(id);
      logger.info("Successfully deleted TodoItem with ID: {}", id);
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while deleting TodoItem: {}", e.getMessage());
      throw e;
    } catch (DataAccessException e) {
      logger.error("Database error while deleting TodoItem with ID: {}", id, e);
      throw new RuntimeException("Failed to delete TODO item from database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while deleting TodoItem with ID: {}", id, e);
      throw new RuntimeException("An unexpected error occurred while deleting TODO item", e);
    }
  }

  /**
   * Cycle the status of a TodoItem to the next status
   */
  public TodoItem cycleStatus(Long id) {
    try {
      if (id == null) {
        throw new IllegalArgumentException("ID cannot be null");
      }

      Optional<TodoItem> optionalItem = repository.findById(id);
      if (optionalItem.isEmpty()) {
        throw new IllegalArgumentException("TodoItem with ID " + id + " not found");
      }

      TodoItem item = optionalItem.get();
      TodoStatus oldStatus = item.getStatus();
      item.setStatus(item.getStatus().next());
      TodoItem updatedItem = repository.save(item);
      logger.info("Successfully cycled status for TodoItem ID {} from {} to {}",
          id, oldStatus, updatedItem.getStatus());
      return updatedItem;
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while cycling status for TodoItem: {}", e.getMessage());
      throw e;
    } catch (DataAccessException e) {
      logger.error("Database error while cycling status for TodoItem with ID: {}", id, e);
      throw new RuntimeException("Failed to update TODO item status in database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while cycling status for TodoItem with ID: {}", id, e);
      throw new RuntimeException("An unexpected error occurred while updating TODO item status", e);
    }
  }

  /**
   * Get all TodoItems with custom sorting
   */
  public List<TodoItem> findAllSorted(Comparator<TodoItem> comparator) {
    try {
      if (comparator == null) {
        throw new IllegalArgumentException("Comparator cannot be null");
      }

      return repository.findAll().stream()
          .sorted(comparator)
          .collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while sorting TodoItems: {}", e.getMessage());
      throw e;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching and sorting TodoItems", e);
      throw new RuntimeException("Failed to retrieve and sort TODO items from database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while fetching and sorting TodoItems", e);
      throw new RuntimeException("An unexpected error occurred while sorting TODO items", e);
    }
  }

  /**
   * Find a TodoItem by ID
   */
  public Optional<TodoItem> findById(Long id) {
    try {
      if (id == null) {
        throw new IllegalArgumentException("ID cannot be null");
      }
      return repository.findById(id);
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while finding TodoItem by ID: {}", e.getMessage());
      throw e;
    } catch (DataAccessException e) {
      logger.error("Database error while finding TodoItem with ID: {}", id, e);
      throw new RuntimeException("Failed to retrieve TODO item from database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while finding TodoItem with ID: {}", id, e);
      throw new RuntimeException("An unexpected error occurred while retrieving TODO item", e);
    }
  }
}
