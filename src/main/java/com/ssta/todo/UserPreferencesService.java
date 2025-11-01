package com.ssta.todo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserPreferencesService {

  private static final Logger logger = LoggerFactory.getLogger(UserPreferencesService.class);
  private static final Long PREFERENCES_ID = 1L;

  private final UserPreferencesRepository repository;

  public UserPreferencesService(UserPreferencesRepository repository) {
    this.repository = repository;
    initializeDefaultPreferences();
  }

  /**
   * Get user preferences, creating default if not exists
   */
  public UserPreferences getPreferences() {
    try {
      Optional<UserPreferences> preferences = repository.findById(PREFERENCES_ID);
      return preferences.orElseGet(this::createDefaultPreferences);
    } catch (DataAccessException e) {
      logger.error("Database error while fetching user preferences", e);
      throw new RuntimeException("Failed to retrieve user preferences from database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while fetching user preferences", e);
      throw new RuntimeException("An unexpected error occurred while retrieving user preferences", e);
    }
  }

  /**
   * Update user preferences
   */
  public UserPreferences updatePreferences(UserPreferences preferences) {
    try {
      if (preferences == null) {
        throw new IllegalArgumentException("Preferences cannot be null");
      }

      // Ensure ID is always 1
      preferences.setId(PREFERENCES_ID);

      // Ensure boolean values are not null
      if (preferences.getShowTodo() == null) {
        preferences.setShowTodo(true);
      }
      if (preferences.getShowInProgress() == null) {
        preferences.setShowInProgress(true);
      }
      if (preferences.getShowComplete() == null) {
        preferences.setShowComplete(true);
      }

      UserPreferences savedPreferences = repository.save(preferences);
      logger.info("Successfully updated user preferences");
      return savedPreferences;
    } catch (IllegalArgumentException e) {
      logger.warn("Validation error while updating preferences: {}", e.getMessage());
      throw e;
    } catch (DataAccessException e) {
      logger.error("Database error while updating user preferences", e);
      throw new RuntimeException("Failed to save user preferences to database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while updating user preferences", e);
      throw new RuntimeException("An unexpected error occurred while saving user preferences", e);
    }
  }

  /**
   * Initialize default preferences on first run
   */
  private void initializeDefaultPreferences() {
    try {
      if (!repository.existsById(PREFERENCES_ID)) {
        createDefaultPreferences();
      }
    } catch (DataAccessException e) {
      logger.error("Database error while initializing default preferences", e);
      // Don't throw - allow application to start even if preferences initialization fails
      logger.warn("Application started without initializing preferences - they will be created on first access");
    } catch (Exception e) {
      logger.error("Unexpected error while initializing default preferences", e);
      // Don't throw - allow application to start
      logger.warn("Application started without initializing preferences - they will be created on first access");
    }
  }

  /**
   * Create and save default preferences
   */
  private UserPreferences createDefaultPreferences() {
    try {
      UserPreferences preferences = new UserPreferences();
      preferences.setId(PREFERENCES_ID);
      preferences.setShowTodo(true);
      preferences.setShowInProgress(true);
      preferences.setShowComplete(true);
      UserPreferences savedPreferences = repository.save(preferences);
      logger.info("Created default user preferences");
      return savedPreferences;
    } catch (DataAccessException e) {
      logger.error("Database error while creating default preferences", e);
      throw new RuntimeException("Failed to create default preferences in database", e);
    } catch (Exception e) {
      logger.error("Unexpected error while creating default preferences", e);
      throw new RuntimeException("An unexpected error occurred while creating default preferences", e);
    }
  }
}
