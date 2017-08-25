<?php
require_once(WP_CONTENT_DIR . '/plugins/versionpress/bootstrap.php');

wp_enqueue_style('versionpress_admin_style', plugins_url( 'css/style.css' , __FILE__ ));
wp_enqueue_style('versionpress_admin_icons', plugins_url( 'icons/style.css' , __FILE__ ));

wp_enqueue_script('versionpress_admin_script', plugins_url( 'js/vp-admin.js' , __FILE__ ));

/**
 * Function executed from Initializer that is given the progress message, decides
 * whether it is suitable for output and if so, calls `show_message()` (WP function).
 *
 * @param string $progressMessage
 */
function _vp_show_progress_message($progressMessage) {

    // We currently only output messages that are defined in InitializerStates
    // which captures the main progress points without too many details

    $initializerStatesReflection = new ReflectionClass('InitializerStates');
    $progressConstantValues = array_values($initializerStatesReflection->getConstants());
    if (in_array($progressMessage, $progressConstantValues)) {
        show_message($progressMessage);
    }
}

?>

<style>
    /* Activation-specific styles need to be defined this way because wp_enqueue_style() puts the style in the footer
       and in some cases, e.g. during the long-running initialization, the footer is not reached until
       all the messages are drawn to the page. This might be improved in the future. */

    .vp-index .welcome-panel {
        padding-bottom: 23px;
        margin-top: 20px;
    }

    .vp-index .welcome-panel p {
        color: inherit;
    }

    .vp-index .welcome-panel .about-description {
        margin: 23px 0 10px;
    }

    .vp-index .welcome-panel ul {
        list-style: none;
        padding-left: 40px;
    }

    .vp-index .welcome-panel ul li {
        position: relative;
    }

    .vp-index .welcome-panel ul .icon {
        position: absolute;
        left: -25px;
        top: 6px;
    }

    .initialization-progress p {
        margin: 1px 0;
    }

    .initialization-done {
        font-size: 1.2em;
        font-weight: bold;
    }

    .vp-index #vp-page-header {
        margin-bottom: 15px;
    }

    tr.disabled {
        opacity: 0.5;
    }

    #vp-service-panel {
        display: none;
    }

    #vp-service-panel.displayed {
        display: block;
    }

    #vp-service-panel-button {
        font-size: 16px;
        float: right;
        padding: 2px 12px;
    }
</style>


<div class="wrap vp-index">

    <?php

    if (isset($_GET['init']) && !vp_is_active()) {
    ?>

        <div class="welcome-panel">

            <div class="welcome-panel-content">

                <h3>VersionPress Activation</h3>


                <p class="about-description">Setting things up for you. It may take a while, please be patient.</p>

                <div class="initialization-progress">
                    <?php
                    global $versionPressContainer;

                    /**
                     * @var Initializer $initializer
                     */
                    $initializer = $versionPressContainer->resolve(VersionPressServices::INITIALIZER);
                    $initializer->onProgressChanged[] = '_vp_show_progress_message';
                    $initializer->initializeVersionPress(); // This is a long-running operation


                    ?>

                </div>

                <p class="initialization-done">All done, we're now redirecting you (or <a href="<?php admin_url('admin.php?page=versionpress/admin/index.php') ?>">click here</a>).</p>
                <?php
                JsRedirect::redirect(admin_url('admin.php?page=versionpress/admin/index.php'), 1000);
                ?>

            </div>


        </div>

        <?php


    } elseif (!vp_is_active()) {
    ?>

        <div class="welcome-panel">

            <div class="welcome-panel-content">

                <h3>Welcome to VersionPress!</h3>

                <p class="about-description">We're ready to make your admin experience better. Click the button below so that VersionPress can initialize itself and start tracking your site. A few things to know:</p>

                <ul>

                    <li>
                        <span class="icon icon-checkmark"></span>
                        VersionPress doesn't require any further setup or interaction. Just activate it and it will start doing its thing.
                    </li>

                    <li>
                        <span class="icon icon-checkmark"></span>
                        To undo a change or roll back to some previous state use the VersionPress item in the left menu.
                    </li>

                    <li>
                        <span class="icon icon-checkmark"></span>
                        VersionPress never sends your data anywhere – it is not a service but an installed thing on your server.
                    </li>

                    <li>
                        <span class="icon icon-notification"></span>
                        You are using a preview version of VersionPress which is suitable for testing purposes only. If you encounter any issues please contact us at <a href="mailto:info@versionpress.net">info@versionpress.net</a>.
                    </li>


                </ul>

                <h4>System requirements check</h4>

                <ul>
                    <?php
                    $requirementsChecker = new RequirementsChecker();
                    $report = $requirementsChecker->getReport();

                    foreach ($report as $requirement) {
                        $iconClass = $requirement["fulfilled"] ? "icon-checkmark" : "icon-warning";
                    ?>
                    <li>
                        <span class="icon <?php echo $iconClass; ?>"></span>
                        <?php echo $requirement["name"]; ?>
                    </li>
                    <?php
                    }

                    if($requirementsChecker->isEverythingFulfilled() && Git::isVersioned(dirname(__FILE__))) {
                    ?>
                        <li>
                            <span class="icon icon-warning"></span>
                            Note: This website is already versioned in Git (the repository is either your custom or has been created by a previous installation of VersionPress). It is not a problem for VersionPress, but be sure you know what you are doing.
                        </li>
                    <?php
                    }
                    ?>
                </ul>

                <div style="text-align: center;">
                <?php
                    if($requirementsChecker->isEverythingFulfilled()) {
                        $activationUrl = admin_url('admin.php?page=versionpress/admin/index.php&init');
                        $buttonClass = "button-primary";
                    } else {
                        $activationUrl = "#";
                        $buttonClass = "button-primary-disabled";
                    }
                ?>
                        <a href="<?php echo $activationUrl; ?>"
                           class="button <?php echo $buttonClass; ?> button-hero" id="activate-versionpress-btn">Activate
                            VersionPress</a>
                </div>
            </div>


        </div>

    <?php
    } else {
        if (isset($_GET['error'])) {
            $errors = array(
                RevertStatus::FAILED => array(
                    'class' => 'error',
                    'message' => 'Error: Overwritten changes can not be reverted.'
                ),
                RevertStatus::NOTHING_TO_COMMIT => array(
                    'class' => 'updated',
                    'message' => 'There was nothing to commit. Current state is the same as the one you want rollback to.'
                ),

            );

            $error = $errors[$_GET['error']];
        }

        if(isset($_GET['bug-report'])) {
            if($_GET['bug-report'] === 'ok') {
                $error = array(
                    'class' => 'updated',
                    'message' => 'Bug report was sent. Thank you.'
                );
            } elseif($_GET['bug-report'] === 'err') {
                $error = array(
                    'class' => 'error',
                    'message' => 'There was a problem with sending bug report. Please try it again. Thank you.'
                );
            }
        }

        if(isset($error)) {
            echo "<div class='$error[class]'><p>$error[message]</p></div>";
        }

        $displayServicePanel = false;
        $displayServicePanel |= isset($_GET['bug-report']);
    ?>

        <button id="vp-service-panel-button"><span class="icon icon-cog"></span></button>
        <h2 id="vp-page-header">VersionPress</h2>

        <div id="vp-service-panel" class="welcome-panel <?php if($displayServicePanel) echo "displayed"; ?>">
            <h3>VersionPress Service Panel</h3>
            <h4>Bug report</h4>
            <form action="<?php echo admin_url('admin-post.php'); ?>" method="post">
                <input type="hidden" name="action" value="vp_send_bug_report">
                <table class="form-table">
                    <tbody>
                        <tr>
                            <th scope="row">
                                <label for="vp-bug-email">Email</label>
                            </th>

                            <td>
                                <input type="email" value="" id="vp-bug-email" name="email">
                                <br>
                                <span class="description">We will respond you to this email.</span>
                            </td>
                        </tr>
                        <tr>
                            <th scope="row">
                                <label for="vp-bug-description">Bug description</label>
                            </th>

                            <td>
                                <textarea rows="4" cols="22" id="vp-bug-description" name="description"></textarea>
                                <br>
                                <span class="description">Please tell us what you were doing when the bug occured.</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <p class="submit">
                    <?php submit_button("Send bug report", "submit", "vp_send_bug_report", false); ?>
                </p>
            </form>
        </div>

        <?php
        $showWelcomePanel = get_user_meta(get_current_user_id(), VersionPressOptions::USER_META_SHOW_WELCOME_PANEL, true);

        if ($showWelcomePanel === "") {
        ?>

            <div id="welcome-panel" class="welcome-panel">

                <a id="vp-welcome-panel-close-button" class="welcome-panel-close" href="">Dismiss</a>

                <div class="welcome-panel-content">

                    <h3>Welcome!</h3>

                    <p class="about-description">Below is the main VersionPress table which will grow as changes are made to this site. You can <strong>Undo</strong> specific changes from the history or <strong>Roll back</strong> the site entirely to a previous state.</p>
                </div>

            </div>

        <?php } ?>

        <table id="versionpress-commits-table" class="wp-list-table widefat fixed posts">
            <tr>
                <th class="manage-column column-date">Date</th>
                <th class="manage-column column-message">Message</th>
                <th class="manage-column column-actions"></th>
            </tr>
            <tbody id="the-list">
            <?php


            $initialCommitHash = trim(file_get_contents(VERSIONPRESS_PLUGIN_DIR . '/.active'));
            $gitLogPaginator = new GitLogPaginator();
            $gitLogPaginator->setCommitsPerPage(5);
            $page = isset($_GET['vp-page']) ? intval($_GET['vp-page']) : 0;
            $commits = $gitLogPaginator->getPage($page);

            $canUndoCommit = Git::wasCreatedAfter($commits[0]->getHash(), $initialCommitHash);
            $isFirstCommit = $page === 0;

            foreach ($commits as $commit) {
                $canUndoCommit = $canUndoCommit && ($commit->getHash() !== $initialCommitHash);
                $canRollbackToThisCommit = !$isFirstCommit && ($canUndoCommit || $commit->getHash() === $initialCommitHash);

                $changeInfo = ChangeInfoMatcher::createMatchingChangeInfo($commit->getMessage());

                $undoSnippet = "<a href='" . admin_url('admin.php?action=vp_undo&commit=' . $commit->getHash()) . "' style='text-decoration:none; white-space:nowrap;' title='Reverts changes done by this commit'>Undo this</a>";

                $rollbackSnippet = "<a href='" . admin_url('admin.php?action=vp_rollback&commit=' . $commit->getHash()) . "' style='text-decoration:none; white-space:nowrap;' title='Reverts site back to this state; effectively undos all the change up to this commit'>Roll back to this</a>";

                $versioningSnippet = "";
                if($canUndoCommit) $versioningSnippet .= $undoSnippet;
                if($canUndoCommit && $canRollbackToThisCommit) $versioningSnippet .= "&nbsp;|&nbsp;";
                if($canRollbackToThisCommit) $versioningSnippet .= $rollbackSnippet;
                $isEnabled = $canUndoCommit || $canRollbackToThisCommit || $commit->getHash() === $initialCommitHash;

                $message = $changeInfo->getChangeDescription();
                echo "
            <tr class=\"post-1 type-post status-publish format-standard hentry category-uncategorized alternate level-0" . ($isEnabled ? "" : " disabled") . "\">
                <td>{$commit->getRelativeDate()}</td>
                <td>$message</td>
                <td style=\"text-align: right\">
                    $versioningSnippet
                </td>
            </tr>";

                $isFirstCommit = false;
            }
            ?>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="3">
                        <?php
                            $pageNumbers = $gitLogPaginator->getPrettySteps($page);

                            $i = 0;
                            $links = "";
                            $lastNumber = 0;

                            foreach($pageNumbers as $pageNumber) {
                                $divider = "";
                                if($i > 0 && $lastNumber != $pageNumber-1) $divider = "&hellip;";
                                elseif($i > 0) $divider = "|";

                                $links .= " " . $divider . " ";
                                $pageUrl = admin_url('admin.php?page=versionpress/admin/index.php&vp-page=' . $pageNumber);
                                if($pageNumber == $page) {
                                    $links .= $pageNumber + 1;
                                } else {
                                    $links .= "<a href=\"$pageUrl\">" . ($pageNumber + 1) . "</a>";
                                }

                                $lastNumber = $pageNumber;
                                $i += 1;
                            }

                            echo $links;
                        ?>
                    </td>
                </tr>
            </tfoot>
        </table>
    <?php
    }
    ?>

</div>