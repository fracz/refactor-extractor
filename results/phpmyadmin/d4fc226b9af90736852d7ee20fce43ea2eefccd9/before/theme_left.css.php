<?php
    // unplanned execution path
    if (!defined('PMA_MINIMUM_COMMON')) {
        exit();
    }
?>
/******************************************************************************/
/* general tags */
<?php if (! empty($GLOBALS['cfg']['FontFamily'])) { ?>
* {
    font-family:        <?php echo $GLOBALS['cfg']['FontFamily']; ?>;
}
<?php } if (! empty($GLOBALS['cfg']['FontSize'])) { ?>
body, table, tbody, tr, td {
    font-size:          <?php echo $GLOBALS['cfg']['FontSize']; ?>;
}
select, input, textarea {
    font-size:          0.7em;
}
<?php } ?>

body, table, tbody, tr, td {
    background-color:   <?php echo $GLOBALS['cfg']['LeftBgColor']; ?>;
    color:              <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
    margin:             0;
    padding:            2px 2px 2px 2px;
}

a img {
    border: 0;
}

form {
    margin:             0;
    padding:            0;
    display:            inline;
}

select {
    background-color:   #ffffff;
    color:              #000000;
    width:              100%;
}

/* buttons in some browsers (eg. Konqueror) are block elements,
   this breaks design */
button {
    display:            inline;
}


/******************************************************************************/
/* classes */

/* leave some space between icons and text */
.icon {
    vertical-align:     middle;
    margin-right:       0.3em;
    margin-left:        0.3em;
}

img.lightbulb {
    cursor:             pointer;
}


/******************************************************************************/
/* specific elements */

div#pmalogo,
div#leftframelinks,
div#databaseList {
    text-align:         center;
    border-bottom:      0.05em solid <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
    margin-bottom:      0.5em;
    padding-bottom:     0.5em;
}

div#leftframelinks .icon {
    padding:            0;
    margin:             0;
}

div#leftframelinks a {
    margin:             0;
    padding:            0.2em;
    border:             0.05em solid <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
}

div#leftframelinks a:hover {
    background-color:   <?php echo $GLOBALS['cfg']['LeftPointerColor']; ?>;
}

/* leftdatabaselist */
div#left_tableList ul {
    list-style-type:    none;
    list-style-position: outside;
    margin:             0;
    padding:            0;
    font-size:          80%;
    background-color:   <?php echo $GLOBALS['cfg']['LeftBgColor']; ?>;
}

div#left_tableList ul ul {
    font-size:          100%;
}

div#left_tableList a {
    color:              <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
    text-decoration:    none;
}

div#left_tableList a:hover {
    color:              <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
    text-decoration:    underline;
}

div#left_tableList li {
    margin:             0;
    padding:            0;
    white-space:        nowrap;
}

<?php if ( $GLOBALS['cfg']['LeftPointerEnable'] ) { ?>
div#left_tableList li:hover {
    background-color:   <?php echo $GLOBALS['cfg']['LeftPointerColor']; ?>;
}
<?php } ?>

div#left_tableList img {
    padding:            0;
    vertical-align:     middle;
}

div#left_tableList ul ul {
    margin-left:        0;
    padding-left:       0.1em;
    border-left:        0.1em solid <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
    padding-bottom:     0.1em;
    border-bottom:      0.1em solid <?php echo $GLOBALS['cfg']['LeftColor']; ?>;
}