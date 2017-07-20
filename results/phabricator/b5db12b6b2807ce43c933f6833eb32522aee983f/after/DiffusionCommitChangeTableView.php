<?php

/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

final class DiffusionCommitChangeTableView extends DiffusionView {

  private $pathChanges;

  public function setPathChanges(array $path_changes) {
    $this->pathChanges = $path_changes;
    return $this;
  }

  public function render() {
    $rows = array();
    foreach ($this->pathChanges as $change) {
      $change_verb = DifferentialChangeType::getFullNameForChangeType(
        $change->getChangeType());

      $rows[] = array(
        $this->linkHistory($change->getPath()),
        $this->linkBrowse($change->getPath()),
        $this->linkChange(
          $change->getPath(),
          array(
            'text' => $change_verb,
          )),
        phutil_escape_html($change->getPath()),
      );
    }

    $view = new AphrontTableView($rows);
    $view->setHeaders(
      array(
        'History',
        'Browse',
        'Change',
        'Path',
      ));
    $view->setColumnClasses(
      array(
        '',
        '',
        '',
        'wide',
      ));
    $view->setNoDataString('This change has not been fully parsed yet.');

    return $view->render();
  }

}