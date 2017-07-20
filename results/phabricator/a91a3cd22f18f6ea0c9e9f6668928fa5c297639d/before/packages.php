<?php

return array(
  'core.pkg.js' => array(
    'javelin-util',
    'javelin-install',
    'javelin-event',
    'javelin-stratcom',
    'javelin-behavior',
    'javelin-resource',
    'javelin-request',
    'javelin-vector',
    'javelin-dom',
    'javelin-json',
    'javelin-uri',
    'javelin-workflow',
    'javelin-mask',
    'javelin-typeahead',
    'javelin-typeahead-normalizer',
    'javelin-typeahead-source',
    'javelin-typeahead-preloaded-source',
    'javelin-typeahead-ondemand-source',
    'javelin-tokenizer',
    'javelin-history',
    'javelin-router',
    'javelin-routable',
    'javelin-behavior-aphront-basic-tokenizer',
    'javelin-behavior-workflow',
    'javelin-behavior-aphront-form-disable-on-submit',
    'phabricator-keyboard-shortcut-manager',
    'phabricator-keyboard-shortcut',
    'javelin-behavior-phabricator-keyboard-shortcuts',
    'javelin-behavior-refresh-csrf',
    'javelin-behavior-phabricator-watch-anchor',
    'javelin-behavior-phabricator-autofocus',
    'phuix-dropdown-menu',
    'phuix-action-list-view',
    'phuix-action-view',
    'phabricator-phtize',
    'javelin-behavior-phabricator-oncopy',
    'phabricator-tooltip',
    'javelin-behavior-phabricator-tooltips',
    'phabricator-prefab',
    'javelin-behavior-device',
    'javelin-behavior-toggle-class',
    'javelin-behavior-lightbox-attachments',
    'phabricator-busy',
    'javelin-aphlict',
    'phabricator-notification',
    'javelin-behavior-aphlict-listen',
    'javelin-behavior-phabricator-search-typeahead',
    'javelin-behavior-aphlict-dropdown',
    'javelin-behavior-history-install',
    'javelin-behavior-phabricator-gesture',
    'javelin-behavior-phabricator-active-nav',
    'javelin-behavior-phabricator-nav',
    'javelin-behavior-phabricator-remarkup-assist',
    'phabricator-textareautils',
    'phabricator-file-upload',
    'javelin-behavior-global-drag-and-drop',
    'javelin-behavior-phabricator-reveal-content',
    'phabricator-hovercard',
    'javelin-behavior-phabricator-hovercards',
    'javelin-color',
    'javelin-fx',
    'phabricator-draggable-list',
    'javelin-behavior-phabricator-transaction-list',
    'javelin-behavior-phabricator-show-older-transactions',
    'javelin-behavior-phui-timeline-dropdown-menu',
    'javelin-behavior-doorkeeper-tag',
    'phabricator-title',
    'javelin-leader',
    'javelin-websocket',
    'javelin-behavior-dashboard-async-panel',
    'javelin-behavior-dashboard-tab-panel',
  ),
  'core.pkg.css' => array(
    'phabricator-core-css',
    'phabricator-zindex-css',
    'phui-button-css',
    'phabricator-standard-page-view',
    'aphront-dialog-view-css',
    'phui-form-view-css',
    'aphront-panel-view-css',
    'aphront-table-view-css',
    'aphront-tokenizer-control-css',
    'aphront-typeahead-control-css',
    'aphront-list-filter-view-css',

    'phabricator-remarkup-css',
    'syntax-highlighting-css',
    'aphront-pager-view-css',
    'phabricator-transaction-view-css',
    'aphront-tooltip-css',
    'phabricator-flag-css',
    'phui-info-view-css',

    'sprite-gradient-css',
    'sprite-menu-css',

    'phabricator-main-menu-view',
    'phabricator-notification-css',
    'phabricator-notification-menu-css',
    'lightbox-attachment-css',
    'phui-header-view-css',
    'phabricator-filetree-view-css',
    'phabricator-nav-view-css',
    'phabricator-side-menu-view-css',
    'phui-crumbs-view-css',
    'phui-object-item-list-view-css',
    'global-drag-and-drop-css',
    'phui-spacing-css',
    'phui-form-css',
    'phui-icon-view-css',

    'phabricator-application-launch-view-css',
    'phabricator-action-list-view-css',
    'phui-property-list-view-css',
    'phui-tag-view-css',
    'phui-list-view-css',

    'font-fontawesome',
    'phui-font-icon-base-css',
    'sprite-main-header-css',
    'phui-box-css',
    'phui-object-box-css',
    'phui-timeline-view-css',
    'sprite-tokens-css',
    'tokens-css',
    'phui-status-list-view-css',

    'phui-feed-story-css',
    'phabricator-feed-css',
    'phabricator-dashboard-css',
    'aphront-multi-column-view-css',
    'phui-action-header-view-css',
  ),
  'differential.pkg.css' => array(
    'differential-core-view-css',
    'differential-changeset-view-css',
    'differential-results-table-css',
    'differential-revision-history-css',
    'differential-revision-list-css',
    'differential-table-of-contents-css',
    'differential-revision-comment-css',
    'differential-revision-add-comment-css',
    'phabricator-object-selector-css',
    'phabricator-content-source-view-css',
    'inline-comment-summary-css',
  ),
  'differential.pkg.js' => array(
    'phabricator-drag-and-drop-file-upload',
    'phabricator-shaped-request',

    'javelin-behavior-differential-feedback-preview',
    'javelin-behavior-differential-edit-inline-comments',
    'javelin-behavior-differential-populate',
    'javelin-behavior-differential-diff-radios',
    'javelin-behavior-differential-comment-jump',
    'javelin-behavior-differential-add-reviewers-and-ccs',
    'javelin-behavior-differential-keyboard-navigation',
    'javelin-behavior-aphront-drag-and-drop-textarea',
    'javelin-behavior-phabricator-object-selector',
    'javelin-behavior-repository-crossreference',
    'javelin-behavior-load-blame',

    'differential-inline-comment-editor',
    'javelin-behavior-differential-dropdown-menus',
    'javelin-behavior-differential-toggle-files',
    'javelin-behavior-differential-user-select',
    'javelin-behavior-aphront-more',
  ),
  'diffusion.pkg.css' => array(
    'diffusion-icons-css',
  ),
  'diffusion.pkg.js' => array(
    'javelin-behavior-diffusion-pull-lastmodified',
    'javelin-behavior-diffusion-commit-graph',
    'javelin-behavior-audit-preview',
  ),
  'maniphest.pkg.css' => array(
    'maniphest-task-summary-css',
  ),
  'maniphest.pkg.js' => array(
    'javelin-behavior-maniphest-batch-selector',
    'javelin-behavior-maniphest-transaction-controls',
    'javelin-behavior-maniphest-transaction-preview',
    'javelin-behavior-maniphest-transaction-expand',
    'javelin-behavior-maniphest-subpriority-editor',
    'javelin-behavior-maniphest-list-editor',
  ),
  'darkconsole.pkg.js' => array(
    'javelin-behavior-dark-console',
    'javelin-behavior-error-log',
  ),
);