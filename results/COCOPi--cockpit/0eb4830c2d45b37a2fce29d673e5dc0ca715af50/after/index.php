{{ $app->assets(['assets:vendor/codemirror/lib/codemirror.js','assets:vendor/codemirror/lib/codemirror.css','assets:vendor/codemirror/theme/monokai.css']) }}

{{ $app->assets(['assets:vendor/codemirror/mode/xml/xml.js']) }}
{{ $app->assets(['assets:vendor/codemirror/mode/htmlmixed/htmlmixed.js']) }}
{{ $app->assets(['assets:vendor/codemirror/mode/javascript/javascript.js']) }}
{{ $app->assets(['assets:vendor/codemirror/mode/css/css.js']) }}
{{ $app->assets(['assets:vendor/codemirror/mode/clike/clike.js']) }}

{{ $app->assets(['assets:vendor/codemirror/addon/edit/matchbrackets.js']) }}
{{ $app->assets(['assets:vendor/codemirror/addon/selection/active-line.js']) }}

{{ $app->assets(['assets:vendor/ajaxupload.js']) }}
{{ $app->assets(['assets:vendor/loadie/jquery.loadie.js', 'assets:vendor/loadie/loadie.css']) }}
{{ $app->assets(['mediamanager:assets/js/index.js']) }}


<div class="app-wrapper" data-ng-controller="mediamanager">

    <div class="uk-navbar">
        <span class="uk-navbar-brand">Mediamanager</span>
        <ul class="uk-navbar-nav">
            <li><a href="" ng-click="action('createfolder')"><i class="uk-icon-plus-circle"></i>&nbsp; Folder</a></li>
            <li><a href="" ng-click="action('createfile')"><i class="uk-icon-plus-circle"></i>&nbsp; File</a></li>
            <li class="media-upload-button">
                <a><i class="uk-icon-upload"></i>&nbsp; </a>
                <form id="frmMediaUpload" action="">
                    <input type="file" name="files[]" onchange="jQuery(this.form).trigger('submit')">
                </form>
            </li>
        </ul>
    </div>
    <br>
    <div class="app-panel">

        <div class="uk-panel app-panel-box docked">
            <ul class="uk-breadcrumb">
                <li ng-click="updatepath('/')"><a href="#/" title="Change dir to root"><i class="uk-icon-home"></i></a></li>
                <li ng-repeat="crumb in breadcrumbs"><a href="#@@ crumb.path @@" ng-click="updatepath(crumb.path)" title="Change dir to @@ crumb.name @@">@@ crumb.name @@</a></li>
            </ul>
        </div>

        <div class="uk-navbar uk-margin-large-bottom">

            <div class="uk-navbar-content">
                <div class="uk-button-group">
                    <button class="uk-button" data-ng-class="mode=='table' ? 'uk-button-danger':''" data-ng-click="(mode='table')" title="Table mode" data-uk-tooltip="{pos:'bottom'}"><i class="uk-icon-list-alt"></i></button>
                    <button class="uk-button" data-ng-class="mode=='list' ? 'uk-button-danger':''" data-ng-click="(mode='list')" title="List mode" data-uk-tooltip="{pos:'bottom'}"><i class="uk-icon-th"></i></button>
                </div>
            </div>

            <div class="uk-navbar-content">
                <span class="uk-alert uk-alert-warning" data-ng-show="dir && (dir.folders.length && viewfilter=='files')"><span class="uk-icon-bolt"></span> <strong>@@ dir.folders.length @@ folders are hidden</strong> via filter</span>
                <span class="uk-alert uk-alert-warning" data-ng-show="dir && (dir.files.length && viewfilter=='folders')"><span class="uk-icon-bolt"></span> <strong>@@ dir.files.length @@ files are hidden</strong> via filter</span>
            </div>
            <div class="uk-navbar-flip">
                <div class="uk-navbar-content uk-form">
                    <div class="uk-form-icon uk-hidden-small">
                        <i class="uk-icon-filter"></i>
                        <input type="text" placeholder="Filter by name..." data-ng-model="namefilter">
                    </div>
                    <div class="uk-button-group">
                        <button class="uk-button" data-ng-class="viewfilter=='all' ? 'uk-button-primary':''" data-ng-click="(viewfilter='all')" title="Show files + directories" data-uk-tooltip="{pos:'bottom'}">All</button>
                        <button class="uk-button" data-ng-class="viewfilter=='folders' ? 'uk-button-primary':''" data-ng-click="(viewfilter='folders')" title="Show only directories" data-uk-tooltip="{pos:'bottom'}"><i class="uk-icon-folder"></i> <span class="uk-text-small">@@dir.folders.length@@</span></button>
                        <button class="uk-button" data-ng-class="viewfilter=='files' ? 'uk-button-primary':''" data-ng-click="(viewfilter='files')" title="Show only files" data-uk-tooltip="{pos:'bottom'}"><i class="uk-icon-file"></i> <span class="uk-text-small">@@dir.files.length@@</span></button>
                    </div>
                </div>
            </div>
        </div>

        <ul class="uk-clearfix media-dir" data-ng-show="mode=='list' && dir && (dir.folders.length || dir.files.length)">
            <li class="uk-width-medium-1-5 uk-width-1-1 uk-float-left" ng-repeat="folder in dir.folders" data-type="folder" data-ng-hide="(viewfilter=='files' || !matchName(folder.name))">
                <div>
                    <div class="mm-type">
                        <i class="uk-icon-folder"></i>
                        <div>
                            <ul class="uk-subnav uk-subnav-line">
                                <li><a ng-click="action('rename', folder)" title="Rename folder"><i class="uk-icon-text-width"></i></a></li>
                                <li><a ng-click="action('remove', folder)" title="Delete folder"><i class="uk-icon-minus-circle"></i></a></li>
                            </ul>
                        </div>
                    </div>
                    <div class="uk-text-truncate mm-caption" title="@@ folder.name @@"><a href="#@@ folder.path @@" ng-click="updatepath(folder.path)">@@ folder.name @@</a></div>
                </div>
            </li>
            <li class="uk-width-medium-1-5 uk-width-1-1 uk-float-left" ng-repeat="file in dir.files" data-ng-hide="(viewfilter=='folders' || !matchName(file.name))">
                <div>
                    <div class="mm-type">
                        <i class="uk-icon-file"></i>
                        <div>
                            <ul class="uk-subnav uk-subnav-line">
                                <li><a ng-click="action('rename', file)" title="Rename file"><i class="uk-icon-text-width"></i></a></li>
                                <li><a ng-click="action('download', file)" title="Download file"><i class="uk-icon-paperclip"></i></a></li>
                                <li><a ng-click="action('remove', file)" title="Delete file"><i class="uk-icon-minus-circle"></i></a></li>
                            </ul>
                        </div>
                    </div>
                    <div class="uk-text-truncate mm-caption" title="@@ file.name @@"><a ng-click="open(file)">@@ file.name @@</a></div>
                </div>
            </li>
        </ul>


        <table class="uk-table uk-table-hover" data-ng-show="mode=='table' && dir && (dir.folders.length || dir.files.length)">
            <thead>
                <tr>
                    <th width="20"></th>
                    <th>Name</th>
                    <th class="uk-text-right">Size</th>
                    <th class="uk-text-right">Lastmodified</th>
                    <th class="uk-text-right">&nbsp;</th>
                </tr>
            </thead>
            <tbody>
                <tr ng-repeat="folder in dir.folders" data-type="folder" data-ng-hide="(viewfilter=='files' || !matchName(folder.name))">
                   <td><i class="uk-icon-folder"></i></td>
                   <td><div class="uk-text-truncate" title="@@ folder.name @@"><a href="#@@ folder.path @@" ng-click="updatepath(folder.path)">@@ folder.name @@</a></div></td>
                   <td>&nbsp;</td>
                   <td>&nbsp;</td>
                   <td class="uk-text-right">
                       <ul class="uk-subnav uk-subnav-line">
                           <li><a ng-click="action('rename', folder)" title="Rename folder"><i class="uk-icon-text-width"></i></a></li>
                           <li><a ng-click="action('remove', folder)" title="Delete folder"><i class="uk-icon-minus-circle"></i></a></li>
                       </ul>
                   </td>
                </tr>

                <tr ng-repeat="file in dir.files" data-type="folder" data-ng-hide="(viewfilter=='folders' || !matchName(file.name))">
                   <td><i class="uk-icon-file"></i></td>
                   <td><div class="uk-text-truncate" title="@@ file.name @@"><a ng-click="open(file)">@@ file.name @@</a></div></td>
                   <td class="uk-text-right">@@ file.size @@</td>
                   <td class="uk-text-right">@@ file.lastmodified @@</td>
                   <td class="uk-text-right">
                       <ul class="uk-subnav uk-subnav-line">
                           <li><a ng-click="action('rename', file)" title="Rename file"><i class="uk-icon-text-width"></i></a></li>
                           <li><a ng-click="action('download', file)" title="Download file"><i class="uk-icon-paperclip"></i></a></li>
                           <li><a ng-click="action('remove', file)" title="Delete file"><i class="uk-icon-minus-circle"></i></a></li>
                       </ul>
                   </td>
                </tr>
            </tbody>
        </table>

        <div class="uk-margin uk-text-center" data-ng-show="dir && (!dir.folders.length && !dir.files.length)">
            <h2><i class="uk-icon-folder-open-o"></i></h2>
            <p class="uk-text-large">
                This folder is empty.
            </p>
        </div>

    </div>

</div>

<div id="mm-image-preview" class="uk-modal">
    <div class="uk-modal-dialog">
        <div class="modal-content uk-text-center"></div>
    </div>
</div>

<div id="mm-editor">
    <nav class="uk-navbar">
        <div class="uk-navbar-content">
            <i class="uk-icon-pencil"></i> &nbsp; <strong class="uk-text-small filename"></strong>
        </div>
        <ul class="uk-navbar-nav">
            <li><a data-editor-action="save" title="Save file" data-uk-tooltip><i class="uk-icon-save"></i></a></li>
        </ul>
        <div class="uk-navbar-flip">
            <ul class="uk-navbar-nav">
                <li><a data-editor-action="close" title="Close file" data-uk-tooltip><i class="uk-icon-times"></i></a></li>
            </ul>
        </div>
    </nav>
    <textarea></textarea>
</div>

<style>

    .app-panel a {
        cursor: pointer;
    }

    .media-dir {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .media-dir > li > div {
        padding: 10px;
        min-height: 100px;
    }

    .media-dir .mm-type {
        position: relative;
        text-align: center;
        padding: 15px;
    }

    .media-dir .mm-type > i {
        font-size: 40px;
    }

    .media-dir .mm-type > div {
        display: none;
        position: absolute;
        top: 45%;
        left: 0;
        right: 0;
    }

    .media-dir .mm-type:hover > div {
        display: block;
    }

    .media-dir .mm-type > div > ul {
        display: inline-block;
        background: #eee;
        background: rgba(0,0,0,0.75);
        padding: 5px 20px;
        border-radius: 3px;
    }

    .media-dir .mm-type > div a {
        color: #fff;
        cursor: pointer;
    }

    table.uk-table .uk-subnav {
        padding: 0;
        margin: 0;
    }

    .media-dir .mm-caption {
        text-align: center;
    }

    .media-upload-button {
        position: relative;
        overflow: hidden;
        cursor: pointer;
    }
    .media-upload-button form {
        opacity: 0;
        position: absolute;
        padding: 0;
        margin: 0;
        top:0;
        left:0;
        font-size: 500px;
    }
    .media-upload-button * { cursor: pointer; }


    /* editor */

    #mm-editor {
        display: none;
        position: fixed;
        left: 0;
        top: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.3);
        border: 10px rgba(0,0,0,0.3) solid;
        z-index: 100;
    }

    #mm-editor .uk-navbar {
        background: #f7f7f7;
        border-radius: 3px 3px 0 0;
    }

    #mm-editor .CodeMirror {
        border: none;
        border-radius:  0 0 3px 3px;
    }

    #mm-editor a { cursor: pointer; }

</style>