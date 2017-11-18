/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.github;

import org.jetbrains.annotations.NotNull;

/**
 * Information about a user on GitHub.
 *
 * @author Kirill Likhodedov
 */
class GithubUser {

  @NotNull private final String myLogin;
  private int myPrivateRepos;
  private int myMaxPrivateRepos;

  GithubUser(@NotNull String login, int privateRepos, int maxPrivateRepos) {
    myLogin = login;
    myPrivateRepos = privateRepos;
    myMaxPrivateRepos = maxPrivateRepos;
  }

  @NotNull
  String getLogin() {
    return myLogin;
  }

  int getMaxPrivateRepos() {
    return myMaxPrivateRepos;
  }

  int getPrivateRepos() {
    return myPrivateRepos;
  }

}