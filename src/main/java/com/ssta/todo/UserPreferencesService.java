package com.ssta.todo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserPreferencesService {

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
    Optional<UserPreferences> preferences = repository.findById(PREFERENCES_ID);
    return preferences.orElseGet(this::createDefaultPreferences);
  }

  /**
   * Update user preferences
   */
  public UserPreferences updatePreferences(UserPreferences preferences) {
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

    return repository.save(preferences);
  }

  /**
   * Initialize default preferences on first run
   */
  private void initializeDefaultPreferences() {
    if (!repository.existsById(PREFERENCES_ID)) {
      createDefaultPreferences();
    }
  }

  /**
   * Create and save default preferences
   */
  private UserPreferences createDefaultPreferences() {
    UserPreferences preferences = new UserPreferences();
    preferences.setId(PREFERENCES_ID);
    preferences.setShowTodo(true);
    preferences.setShowInProgress(true);
    preferences.setShowComplete(true);
    return repository.save(preferences);
  }
}
