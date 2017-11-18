package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class KoalaUtils {
  private KoalaUtils() {}

  @NonNull public static Map<String, Object> discoveryParamsProperties(@NonNull final DiscoveryParams params) {
    return discoveryParamsProperties(params, "discover_");
  }

  @NonNull public static Map<String, Object> discoveryParamsProperties(@NonNull final DiscoveryParams params, @NonNull String prefix) {

    final Map<String, Object> properties = Collections.unmodifiableMap(new HashMap<String, Object>() {{

      put("staff_picks", params.staffPicks());
      put("starred", params.starred());
      put("social", params.social());
      put("term", params.term());
      put("sort", params.sort());
      put("page", params.page());
      put("per_page", params.perPage());

      final Category category = params.category();
      if (category != null) {
        putAll(categoryProperties(category));
      }

      final Location location = params.location();
      if (location != null) {
        putAll(locationProperties(location));
      }
    }});

    return MapUtils.prefixKeys(properties, prefix);
  }

  @NonNull public static Map<String, Object> categoryProperties(@NonNull final Category category) {
    return categoryProperties(category, "category_");
  }

  @NonNull public static Map<String, Object> categoryProperties(@NonNull final Category category, @NonNull final String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {{
      put("id", String.valueOf(category.id()));
      put("name", String.valueOf(category.name()));
    }};

    return MapUtils.prefixKeys(properties, prefix);
  }

  @NonNull public static Map<String, Object> locationProperties(@NonNull final Location location) {
    return locationProperties(location, "location_");
  }

  @NonNull public static Map<String, Object> locationProperties(@NonNull final Location location, @NonNull final String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {{

      put("id", location.id());
      put("name", location.name());
      put("displayable_name", location.displayableName());
      put("city", location.city());
      put("state", location.state());
      put("country", location.country());
      put("projects_count", location.projectsCount());
    }};

    return MapUtils.prefixKeys(properties, prefix);
  }

  @NonNull public static Map<String, Object> userProperties(@NonNull final User user) {
    return userProperties(user, "user_");
  }

  @NonNull public static Map<String, Object> userProperties(@NonNull final User user, @NonNull final String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {{
      put("uid", user.id());
      put("backed_projects_count", user.backedProjectsCount());
      put("created_projects_count", user.createdProjectsCount());
      put("starred_projects_count", user.starredProjectsCount());
    }};

    return MapUtils.prefixKeys(properties, prefix);
  }

  @NonNull public static Map<String, Object> projectProperties(@NonNull final Project project) {
    return projectProperties(project, "project_");
  }

  @NonNull public static Map<String, Object> projectProperties(@NonNull final Project project, @NonNull final String prefix) {
    return projectProperties(project, null, prefix);
  }

  @NonNull public static Map<String, Object> projectProperties(@NonNull final Project project, @Nullable final User loggedInUser, @NonNull final String prefix) {
    final Map<String, Object> properties = new HashMap<String, Object>() {{

      put("backers_count", project.backersCount());
      put("country", project.country());
      put("currency", project.currency());
      put("goal", project.goal());
      put("pid", project.id());
      put("name", project.name());
      put("state", project.state());
      put("update_count", project.updatesCount());
      put("comments_count", project.commentsCount());
      put("pledged", project.pledged());
      put("percent_raised", project.percentageFunded() / 100.0f);
      put("has_video", project.video() != null);
      put("hours_remaining", ProjectUtils.timeInSecondsUntilDeadline(project) / 60.0f / 60.0f);

      // TODO: Implement `duration`
      // put("duration", project.duration());

      final Category category = project.category();
      if (category != null) {
        put("category", category.name());
        final Category parent = category.parent();
        if (parent != null) {
          put("parent_category", parent.name());
        }
      }

      final Location location = project.location();
      if (location != null) {
        put("location", location.name());
      }

      putAll(userProperties(project.creator(), "creator_"));

      if (loggedInUser != null) {
        put("user_is_project_creator", ProjectUtils.userIsCreator(project, loggedInUser));
        put("user_is_backer", project.isBacking());
        put("user_has_starred", project.isStarred());
      }
    }};

    return MapUtils.prefixKeys(properties, prefix);
  }

  @NonNull public static Map<String, Object> activityProperties(@NonNull final Activity activity) {
    return activityProperties(activity, "activity_");
  }

  @NonNull public static Map<String, Object> activityProperties(@NonNull final Activity activity, @NonNull final String prefix) {
    Map<String, Object> properties = new HashMap<String, Object>() {{
      put("category", activity.category());
    }};

    properties = MapUtils.prefixKeys(properties, prefix);

    final Project project = activity.project();
    if (project != null) {
      properties.putAll(projectProperties(project));

      final Update update = activity.update();
      if (update != null) {
        properties.putAll(updateProperties(project, update));
      }
    }

    return properties;
  }

  @NonNull public static Map<String, Object> updateProperties(@NonNull final Project project, @NonNull final Update update) {
    return updateProperties(project, update, "update_");
  }

  @NonNull public static Map<String, Object> updateProperties(@NonNull final Project project, @NonNull final Update update, @NonNull final String prefix) {
    Map<String, Object> properties = new HashMap<String, Object>() {{
      put("id", update.id());
      put("title", update.title());
      put("visible", update.visible());
      put("comments_count", update.commentsCount());

      // TODO: add `public` to `Update` model
      // put("public")
      // TODO: how to convert update.publishedAt() to seconds since 1970
      // put("published_at", update.publishedAt())
    }};

    properties = MapUtils.prefixKeys(properties, prefix);

    properties.putAll(projectProperties(project));

    return properties;
  }
}