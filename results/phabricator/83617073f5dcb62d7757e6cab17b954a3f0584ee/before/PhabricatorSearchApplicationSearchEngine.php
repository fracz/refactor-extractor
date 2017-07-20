<?php

final class PhabricatorSearchApplicationSearchEngine
  extends PhabricatorApplicationSearchEngine {

  public function getResultTypeDescription() {
    return pht('Fulltext Results');
  }

  public function getApplicationClassName() {
    return 'PhabricatorSearchApplication';
  }

  public function buildSavedQueryFromRequest(AphrontRequest $request) {
    $saved = new PhabricatorSavedQuery();

    $saved->setParameter('query', $request->getStr('query'));
    $saved->setParameter(
      'statuses',
      $this->readListFromRequest($request, 'statuses'));
    $saved->setParameter(
      'types',
      $this->readListFromRequest($request, 'types'));

    $saved->setParameter(
      'authorPHIDs',
      $this->readUsersFromRequest($request, 'authorPHIDs'));

    $saved->setParameter(
      'ownerPHIDs',
      $this->readUsersFromRequest($request, 'ownerPHIDs'));

    $saved->setParameter(
      'subscriberPHIDs',
      $this->readSubscribersFromRequest($request, 'subscriberPHIDs'));

    $saved->setParameter(
      'projectPHIDs',
      $this->readPHIDsFromRequest($request, 'projectPHIDs'));

    return $saved;
  }

  public function buildQueryFromSavedQuery(PhabricatorSavedQuery $saved) {
    $query = new PhabricatorSearchDocumentQuery();

    // Convert the saved query into a resolved form (without typeahead
    // functions) which the fulltext search engines can execute.
    $config = clone $saved;
    $viewer = $this->requireViewer();

    $datasource = id(new PhabricatorPeopleOwnerDatasource())
      ->setViewer($viewer);
    $owner_phids = $this->readOwnerPHIDs($config);
    $owner_phids = $datasource->evaluateTokens($owner_phids);
    foreach ($owner_phids as $key => $phid) {
      if ($phid == PhabricatorPeopleNoOwnerDatasource::FUNCTION_TOKEN) {
        $config->setParameter('withUnowned', true);
        unset($owner_phids[$key]);
      }
    }
    $config->setParameter('ownerPHIDs', $owner_phids);


    $datasource = id(new PhabricatorTypeaheadUserParameterizedDatasource())
      ->setViewer($viewer);
    $author_phids = $config->getParameter('authorPHIDs', array());
    $author_phids = $datasource->evaluateTokens($author_phids);
    $config->setParameter('authorPHIDs', $author_phids);


    $datasource = id(new PhabricatorMetaMTAMailableFunctionDatasource())
      ->setViewer($viewer);
    $subscriber_phids = $config->getParameter('subscriberPHIDs', array());
    $subscriber_phids = $datasource->evaluateTokens($subscriber_phids);
    $config->setParameter('subscriberPHIDs', $subscriber_phids);


    $query->withSavedQuery($config);

    return $query;
  }

  public function buildSearchForm(
    AphrontFormView $form,
    PhabricatorSavedQuery $saved) {

    $options = array();
    $author_value = null;
    $owner_value = null;
    $subscribers_value = null;
    $project_value = null;

    $author_phids = $saved->getParameter('authorPHIDs', array());
    $owner_phids = $this->readOwnerPHIDs($saved);
    $subscriber_phids = $saved->getParameter('subscriberPHIDs', array());
    $project_phids = $saved->getParameter('projectPHIDs', array());

    $status_values = $saved->getParameter('statuses', array());
    $status_values = array_fuse($status_values);

    $statuses = array(
      PhabricatorSearchRelationship::RELATIONSHIP_OPEN => pht('Open'),
      PhabricatorSearchRelationship::RELATIONSHIP_CLOSED => pht('Closed'),
    );
    $status_control = id(new AphrontFormCheckboxControl())
      ->setLabel(pht('Document Status'));
    foreach ($statuses as $status => $name) {
      $status_control->addCheckbox(
        'statuses[]',
        $status,
        $name,
        isset($status_values[$status]));
    }

    $type_values = $saved->getParameter('types', array());
    $type_values = array_fuse($type_values);

    $types_control = id(new AphrontFormTokenizerControl())
      ->setLabel(pht('Document Types'))
      ->setName('types')
      ->setDatasource(new PhabricatorSearchDocumentTypeDatasource())
      ->setValue($type_values);

    $form
      ->appendChild(
        phutil_tag(
          'input',
          array(
            'type' => 'hidden',
            'name' => 'jump',
            'value' => 'no',
          )))
      ->appendChild(
        id(new AphrontFormTextControl())
          ->setLabel('Query')
          ->setName('query')
          ->setValue($saved->getParameter('query')))
      ->appendChild($status_control)
      ->appendControl($types_control)
      ->appendControl(
        id(new AphrontFormTokenizerControl())
          ->setName('authorPHIDs')
          ->setLabel('Authors')
          ->setDatasource(new PhabricatorTypeaheadUserParameterizedDatasource())
          ->setValue($author_phids))
      ->appendControl(
        id(new AphrontFormTokenizerControl())
          ->setName('ownerPHIDs')
          ->setLabel('Owners')
          ->setDatasource(new PhabricatorPeopleOwnerDatasource())
          ->setValue($owner_phids))
      ->appendControl(
        id(new AphrontFormTokenizerControl())
          ->setName('subscriberPHIDs')
          ->setLabel('Subscribers')
          ->setDatasource(new PhabricatorMetaMTAMailableFunctionDatasource())
          ->setValue($subscriber_phids))
      ->appendControl(
        id(new AphrontFormTokenizerControl())
          ->setName('projectPHIDs')
          ->setLabel('In Any Project')
          ->setDatasource(new PhabricatorProjectDatasource())
          ->setValue($project_phids));
  }

  protected function getURI($path) {
    return '/search/'.$path;
  }

  protected function getBuiltinQueryNames() {
    return array(
      'all' => pht('All Documents'),
      'open' => pht('Open Documents'),
      'open-tasks' => pht('Open Tasks'),
    );
  }

  public function buildSavedQueryFromBuiltin($query_key) {
    $query = $this->newSavedQuery();
    $query->setQueryKey($query_key);

    switch ($query_key) {
      case 'all':
        return $query;
      case 'open':
        return $query->setParameter('statuses', array('open'));
      case 'open-tasks':
        return $query
          ->setParameter('statuses', array('open'))
          ->setParameter('types', array(ManiphestTaskPHIDType::TYPECONST));
    }

    return parent::buildSavedQueryFromBuiltin($query_key);
  }

  public static function getIndexableDocumentTypes(
    PhabricatorUser $viewer = null) {

    // TODO: This is inelegant and not very efficient, but gets us reasonable
    // results. It would be nice to do this more elegantly.

    $indexers = id(new PhutilSymbolLoader())
      ->setAncestorClass('PhabricatorSearchDocumentIndexer')
      ->loadObjects();

    if ($viewer) {
      $types = PhabricatorPHIDType::getAllInstalledTypes($viewer);
    } else {
      $types = PhabricatorPHIDType::getAllTypes();
    }

    $results = array();
    foreach ($types as $type) {
      $typeconst = $type->getTypeConstant();
      foreach ($indexers as $indexer) {
        $fake_phid = 'PHID-'.$typeconst.'-fake';
        if ($indexer->shouldIndexDocumentByPHID($fake_phid)) {
          $results[$typeconst] = $type->getTypeName();
        }
      }
    }

    asort($results);

    return $results;
  }

  public function shouldUseOffsetPaging() {
    return true;
  }

  protected function renderResultList(
    array $results,
    PhabricatorSavedQuery $query,
    array $handles) {

    $viewer = $this->requireViewer();

    if ($results) {
      $objects = id(new PhabricatorObjectQuery())
        ->setViewer($viewer)
        ->withPHIDs(mpull($results, 'getPHID'))
        ->execute();

      $list = new PHUIObjectItemListView();
      foreach ($results as $phid => $handle) {
        $view = id(new PhabricatorSearchResultView())
          ->setHandle($handle)
          ->setQuery($query)
          ->setObject(idx($objects, $phid))
          ->render();
        $list->addItem($view);
      }

      $results = $list;
    } else {
      $results = id(new PHUIInfoView())
        ->appendChild(pht('No results returned for that query.'))
        ->setSeverity(PHUIInfoView::SEVERITY_NODATA);
    }

    return $results;
  }

  private function readOwnerPHIDs(PhabricatorSavedQuery $saved) {
    $owner_phids = $saved->getParameter('ownerPHIDs', array());

    // This was an old checkbox from before typeahead functions.
    if ($saved->getParameter('withUnowned')) {
      $owner_phids[] = PhabricatorPeopleNoOwnerDatasource::FUNCTION_TOKEN;
    }

    return $owner_phids;
  }

}