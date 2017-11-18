package com.kickstarter.factories;

import com.kickstarter.models.User;

public final class UserFactory {
  private UserFactory() {}

  public static User user() {
    return User.builder()
      .avatar(AvatarFactory.avatar())
      .id(1234567890)
      .name("Sammy Sosa")
      .location(LocationFactory.unitedStates())
      .build();
  }

  public static User socialUser() {
    return user().toBuilder().social(true).build();
  }

  public static User creator() {
    return user()
      .toBuilder()
      .createdProjectsCount(5)
      .build();
  }

  public static User germanUser() {
    return user()
      .toBuilder()
      .location(LocationFactory.germany())
      .build();
  }
}