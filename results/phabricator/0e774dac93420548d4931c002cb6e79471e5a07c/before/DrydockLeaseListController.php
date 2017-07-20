<?php

final class DrydockLeaseListController extends DrydockController {

  public function processRequest() {
    $request = $this->getRequest();
    $user = $request->getUser();

    $nav = $this->buildSideNav('lease');

    $pager = new AphrontPagerView();
    $pager->setURI(new PhutilURI('/drydock/lease/'), 'page');

    $data = id(new DrydockLease())->loadAllWhere(
      '1 = 1 ORDER BY id DESC LIMIT %d, %d',
      $pager->getOffset(),
      $pager->getPageSize() + 1);
    $data = $pager->sliceResults($data);

    $phids = mpull($data, 'getOwnerPHID');
    $handles = $this->loadViewerHandles($phids);

    $resource_ids = mpull($data, 'getResourceID');
    $resources = array();
    if ($resource_ids) {
      $resources = id(new DrydockResource())->loadAllWhere(
        'id IN (%Ld)',
        $resource_ids);
    }

    $rows = array();
    foreach ($data as $lease) {
      $resource = idx($resources, $lease->getResourceID());
      $rows[] = array(
        $lease->getID(),
        DrydockLeaseStatus::getNameForStatus($lease->getStatus()),
        ($lease->getOwnerPHID()
          ? $handles[$lease->getOwnerPHID()]->renderLink()
          : null),
        $lease->getResourceID(),
        ($resource
          ? phutil_escape_html($resource->getName())
          : null),
        phabricator_datetime($lease->getDateCreated(), $user),
      );
    }

    $table = new AphrontTableView($rows);
    $table->setHeaders(
      array(
        'ID',
        'Status',
        'Owner',
        'Resource ID',
        'Resource',
        'Created',
      ));
    $table->setColumnClasses(
      array(
        '',
        '',
        '',
        '',
        'wide pri',
        'right',
      ));

    $panel = new AphrontPanelView();
    $panel->setHeader('Drydock Leases');

    $panel->appendChild($table);
    $panel->appendChild($pager);

    $nav->appendChild($panel);
    return $this->buildStandardPageResponse(
      $nav,
      array(
        'title' => 'Leases',
      ));

  }

}