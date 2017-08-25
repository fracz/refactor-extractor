<?  // $Id$
    // social.php - course format featuring social forum
    //              included from view.php

    include("../mod/discuss/lib.php");
    include("../mod/reading/lib.php");
?>

<TABLE WIDTH="100%" BORDER="0" CELLSPACING="5" CELLPADDING="5">
  <TR>
    <TD WIDTH="15%" VALIGN="TOP">
      <?
      //print_simple_box("News", $align="CENTER", $width="100%", $color="$THEME->cellheading");

      //print_side_block("<A HREF=\"new.php?id=$course->id\">What's New!</A>",
      //                 "", "<FONT SIZE=1>...since your last login</FONT>");
      //if ($news = get_course_news_forum($course->id)) {
          //forum_latest_topics($news->id, 5, "minimal", "DESC", false);
      //}

      //echo "<BR><BR>";
      print_simple_box("People", $align="CENTER", $width="100%", $color="$THEME->cellheading");
      $moddata[]="<A HREF=\"../user/index.php?id=$course->id\">Participants</A>";
      $modicon[]="<IMG SRC=\"../user/users.gif\" HEIGHT=16 WIDTH=16 ALT=\"List of everyone\">";
      $moddata[]="<A HREF=\"../user/view.php?id=$USER->id&course=$course->id\">Edit my profile</A>";
      $modicon[]="<IMG SRC=\"../user/user.gif\" HEIGHT=16 WIDTH=16 ALT=\"Me\">";
      print_side_block("", $moddata, "", $modicon);


      // Then, print all the available readings

      print_simple_box("Readings", $align="CENTER", $width="100%", $color="$THEME->cellheading");

      if ($readings = list_all_readings($course->id, "timemodified ASC", 0, true)) {
         foreach ($readings as $reading) {
             $readingdata[] = $reading;
             $readingicon[] = "<IMG SRC=\"../mod/reading/icon.gif\" HEIGHT=16 WIDTH=16 ALT=\"Reading\">";
         }
         if ($USER->editing) {
             $readingdata[] = "<A HREF=\"mod.php?id=$course->id&week=0&add=reading\">Add reading...</A>";
             $readingicon[] = "&nbsp;";
         }
      }
      print_side_block("", $readingdata, "", $readingicon);

      if (isteacher($USER->id)) {
          print_simple_box("Admin", $align="CENTER", $width="100%", $color="$THEME->cellheading");
          $adminicon[]="<IMG SRC=\"../pix/i/edit.gif\" HEIGHT=16 WIDTH=16 ALT=\"Edit\">";
          if ($USER->editing) {
              $admindata[]="<A HREF=\"view.php?id=$course->id&edit=off\">Turn editing off</A>";
          } else {
              $admindata[]="<A HREF=\"view.php?id=$course->id&edit=on\">Turn editing on</A>";
          }

          $admindata[]="<A HREF=\"edit.php?id=$course->id\">Course settings...</A>";
          $adminicon[]="<IMG SRC=\"../pix/i/settings.gif\" HEIGHT=16 WIDTH=16 ALT=\"Course\">";
          $admindata[]="<A HREF=\"log.php?id=$course->id\">Logs...</A>";
          $adminicon[]="<IMG SRC=\"../pix/i/log.gif\" HEIGHT=16 WIDTH=16 ALT=\"Log\">";
          $admindata[]="<A HREF=\"../files/index.php?id=$course->id\">Files...</A>";
          $adminicon[]="<IMG SRC=\"../files/pix/files.gif\" HEIGHT=16 WIDTH=16 ALT=\"Files\">";

          print_side_block("", $admindata, "", $adminicon);

      }
      ?>

    </TD>

    <TD WIDTH="55%" VALIGN="TOP">
      <?
      if (!$social = get_course_social_forum($course->id)) {
          error("Could not find or create a social forum here");
      }

      $SESSION->fromdiscuss = "$CFG->wwwdir/course/view.php?id=$course->id";
      if (is_subscribed($USER->id, $social->id)) {
          $subtext = "Unsubscribe";
      } else {
          $subtext = "Subscribe me by mail";
      }
      $headertext = "<TABLE BORDER=0 WIDTH=100% CELLPADDING=0 CELLSPACING=0><TR><TD>Discussion Topics<TD ALIGN=RIGHT><FONT SIZE=1><A HREF=\"../mod/discuss/subscribe.php?id=$social->id\">$subtext</A></TD></TR></TABLE>";
         print_simple_box("$headertext", $align="CENTER", $width="100%", $color="$THEME->cellheading"); ?>
      <IMG HEIGHT=7 SRC="../pix/spacer.gif"><BR>

      <?
          if ($social = get_course_social_forum($course->id)) {
              forum_latest_topics($social->id, 5, "plain", "DESC", false);
          } else {
              error("Could not find or create a social forum here");
          }
      ?>

    </TD>
  </TR>
</TABLE>
