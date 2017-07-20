<?php

final class PhabricatorPasteListController extends PhabricatorPasteController {

  public function shouldRequireLogin() {
    return false;
  }

  private $filter;
  private $queryKey;

  public function willProcessRequest(array $data) {
    $this->filter = idx($data, 'filter');
    $this->queryKey = idx($data, 'queryKey');
  }

  public function processRequest() {
    $request = $this->getRequest();
    $user = $request->getUser();

    if ($request->isFormPost()) {
      $saved = id(new PhabricatorPasteSearchEngine())
        ->buildSavedQueryFromRequest($request);
      if (count($saved->getParameter('authorPHIDs')) == 0) {
        return id(new AphrontRedirectResponse())
          ->setURI('/paste/filter/advanced/');
      }
      return id(new AphrontRedirectResponse())
        ->setURI('/paste/query/'.$saved->getQueryKey().'/');
    }

    $nav = $this->buildSideNavView($this->filter);
    $filter = $nav->getSelectedFilter();

    $saved_query = new PhabricatorSavedQuery();
    $engine = id(new PhabricatorPasteSearchEngine())
      ->setPasteSearchFilter($filter)
      ->setPasteSearchUser($request->getUser());

    if ($this->queryKey !== null) {
      $saved_query = id(new PhabricatorSavedQuery())->loadOneWhere(
        'queryKey = %s',
        $this->queryKey);

      if (!$saved_query) {
        return new Aphront404Response();
      }

      $query = id(new PhabricatorPasteSearchEngine())
        ->buildQueryFromSavedQuery($saved_query);
    } else {
      $saved_query = $engine->buildSavedQueryFromRequest($request);
      $query = $engine->buildQueryFromSavedQuery($saved_query);
    }

    $pager = new AphrontCursorPagerView();
    $pager->readFromRequest($request);
    $pastes = $query->setViewer($request->getUser())
      ->needContent(true)
      ->executeWithCursorPager($pager);

    $list = $this->buildPasteList($pastes);
    $list->setPager($pager);
    $list->setNoDataString(pht("No results found for this query."));

    if ($this->queryKey !== null || $filter == "advanced") {
      $form = $engine->buildSearchForm($saved_query);
      $nav->appendChild(
        array(
          $form
        ));
    }

    $nav->appendChild(
      array(
        $list,
      ));

    $crumbs = $this
      ->buildApplicationCrumbs($nav)
      ->addCrumb(
        id(new PhabricatorCrumbView())
          ->setName(pht("Pastes"))
          ->setHref($this->getApplicationURI('filter/'.$filter.'/')));

    $nav->setCrumbs($crumbs);

    return $this->buildApplicationPage(
      $nav,
      array(
        'title' => pht("Pastes"),
        'device' => true,
        'dust' => true,
      ));
  }

  private function buildPasteList(array $pastes) {
    assert_instances_of($pastes, 'PhabricatorPaste');

    $user = $this->getRequest()->getUser();

    $this->loadHandles(mpull($pastes, 'getAuthorPHID'));

    $lang_map = PhabricatorEnv::getEnvConfig('pygments.dropdown-choices');

    $list = new PhabricatorObjectItemListView();
    $list->setUser($user);
    foreach ($pastes as $paste) {
      $created = phabricator_date($paste->getDateCreated(), $user);
      $author = $this->getHandle($paste->getAuthorPHID())->renderLink();
      $source_code = $this->buildSourceCodeView($paste, 5)->render();

      $source_code = phutil_tag(
        'div',
        array(
          'class' => 'phabricator-source-code-summary',
        ),
        $source_code);

      $line_count = count(explode("\n", $paste->getContent()));
      $line_count = pht(
        '%s Line(s)',
        new PhutilNumber($line_count));

      $title = nonempty($paste->getTitle(), pht('(An Untitled Masterwork)'));

      $item = id(new PhabricatorObjectItemView())
        ->setObjectName('P'.$paste->getID())
        ->setHeader($title)
        ->setHref('/P'.$paste->getID())
        ->setObject($paste)
        ->addByline(pht('Author: %s', $author))
        ->addIcon('none', $line_count)
        ->appendChild($source_code);

      $lang_name = $paste->getLanguage();
      if ($lang_name) {
        $lang_name = idx($lang_map, $lang_name, $lang_name);
        $item->addIcon('none', $lang_name);
      }

      $list->addItem($item);
    }

    return $list;
  }

}