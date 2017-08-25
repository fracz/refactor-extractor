<?php
if ($iframed) {
?>
<link rel="stylesheet" href="<?php echo AmpConfig::get('web_path'); ?>/templates/jplayer.midnight.black-iframed.css" type="text/css" />
<?php
} else {
?>
<?php require_once AmpConfig::get('prefix') . '/templates/stylesheets.inc.php'; ?>
<link rel="stylesheet" href="<?php echo AmpConfig::get('web_path'); ?>/templates/jplayer.midnight.black.css" type="text/css" />
<?php
}
?>
<script src="<?php echo AmpConfig::get('web_path'); ?>/modules/jquery/jquery.min.js" language="javascript" type="text/javascript"></script>
<script src="<?php echo AmpConfig::get('web_path'); ?>/modules/jquery/jquery.cookie.js" language="javascript" type="text/javascript"></script>
<script src="<?php echo AmpConfig::get('web_path'); ?>/modules/jplayer/jquery.jplayer.min.js" language="javascript" type="text/javascript"></script>
<script src="<?php echo AmpConfig::get('web_path'); ?>/modules/jplayer/jplayer.playlist.min.js" language="javascript" type="text/javascript"></script>
<?php
if ($iframed) {
?>
<script type="text/javascript">
function NavigateTo(url)
{
    window.parent.document.getElementById('frame_main').setAttribute('src', url);
}
</script>
<?php
}
?>
<script type="text/javascript">
var jplaylist = null;
var timeoffset = 0;

    $(document).ready(function(){

        if (!isNaN($.cookie('jp_volume'))) {
            var jp_volume = $.cookie('jp_volume');
        } else {
            var jp_volume = 0.80;
        }

        jplaylist = new jPlayerPlaylist({
            jPlayer: "#jquery_jplayer_1",
            cssSelectorAncestor: "#jp_container_1"
        }, [], {
            playlistOptions: {
                autoPlay: true,
                loopOnPrevious: false,
                shuffleOnLoop: true,
                enableRemoveControls: false,
                displayTime: 'slow',
                addTime: 'fast',
                removeTime: 'fast',
                shuffleTime: 'slow'
            },
            swfPath: "<?php echo AmpConfig::get('web_path'); ?>/modules/jplayer/",
            audioFullScreen: true,
            smoothPlayBar: true,
            keyEnabled: true,
            solution: "<?php
$solutions = array();
if (AmpConfig::get('webplayer_html5')) {
    $solutions[] = 'html';
}
if (AmpConfig::get('webplayer_flash')) {
    $solutions[] = 'flash';
}
echo implode(',', $solutions);
?>",
            nativeSupport:true,
            oggSupport: false,
            supplied: "<?php echo implode(", ", WebPlayer::get_supplied_types($playlist)); ?>",
            volume: jp_volume,
            size: {
<?php
if ($isVideo) {
    if ($iframed) {
?>
                width: "142px",
                height: "80px",
<?php
} else {
?>
                width: "192px",
                height: "108px",
<?php
}
?>
                cssClass: "jp-video-360p"
<?php
} else {
    if ($iframed) {
?>
                width: "80px",
                height: "80px",
<?php
} else {
?>
                width: "200px",
                height: "auto",
<?php
    }
}
?>
            }
        });

    $("#jquery_jplayer_1").bind($.jPlayer.event.play, function (event) {
        var current = jplaylist.current,
            playlist = jplaylist.playlist;
        var pos = $(".jp-playlist-current").position().top + $(".jp-playlist").scrollTop();
        $(".jp-playlist").scrollTop(pos);
        $.each(playlist, function (index, obj) {
            if (index == current) {
<?php
if (!$isVideo) {
    if ($iframed) {
        echo "var titleobj = (albumids[index] != null) ? '<a href=\"javascript:NavigateTo(\'" . AmpConfig::get('web_path') . "/albums.php?action=show&album=' + albumids[index] + '\');\">' + obj.title + '</a>' : obj.title;";
        echo "var artistobj = '<a href=\"javascript:NavigateTo(\'" . AmpConfig::get('web_path') . "/artists.php?action=show&artist=' + artistids[index] + '\');\">' + obj.artist + '</a>';";
        echo "var lyricsobj = '<a href=\"javascript:NavigateTo(\'" . AmpConfig::get('web_path') . "/song.php?action=show_lyrics&song_id=' + songids[index] + '\');\">" . T_('Show Lyrics') . "</a>';";
        echo "var actionsobj = '|';";
        if (AmpConfig::get('sociable')) {
            echo "actionsobj += ' <a href=\"javascript:NavigateTo(\'" . AmpConfig::get('web_path') . "/shout.php?action=show_add_shout&type=song&id=' + songids[index] + '\');\">" . UI::get_icon('comment', T_('Post Shout')) . "</a>';";
        }
        if (AmpConfig::get('waveform')) {
            echo "var waveformobj = '';";
            if (AmpConfig::get('waveform')) {
                echo "waveformobj += '<a href=\"#\" title=\"" . T_('Post Shout') . "\" onClick=\"javascript:NavigateTo(\'" . AmpConfig::get('web_path') . "/shout.php?action=show_add_shout&type=song&id=' + songids[index] + '&offset=\' + clickTimeOffset(event));\">';";
            }
            echo "waveformobj += '<div class=\"waveform-time\"></div><img src=\"" . AmpConfig::get('web_path') . "/waveform.php?song_id=' + songids[index] + '\" onLoad=\"ShowWaveform();\">';";
            if (AmpConfig::get('waveform')) {
                echo "waveformobj += '</a>';";
            }
        }
    } else {
        echo "var titleobj = obj.title;";
        echo "var artistobj = obj.artist;";
    }
?>
                $('.playing_title').html(titleobj);
                $('.playing_artist').html(artistobj);
<?php
    if ($iframed) {
?>
                $('.playing_actions').html(actionsobj);
<?php
        if (AmpConfig::get('show_lyrics')) {
?>
                $('.playing_lyrics').html(lyricsobj);
<?php
        }
        if (AmpConfig::get('waveform')) {
?>
                $('.waveform').html(waveformobj);
<?php
        }
    }
}
if (AmpConfig::get('song_page_title')) {
    if ($iframed) {
        echo "window.parent.document";
    } else {
        echo "document";
    }
    echo ".title = obj.title + ' - ' + obj.artist + ' | " . addslashes(AmpConfig::get('site_title')) . "';";
}
?>
            }
        });
<?php
    if (AmpConfig::get('waveform')) {
?>
        HideWaveform();
<?php } ?>
    });

<?php
if ($isVideo) {
?>
    $("a.jp-full-screen").on('click', function() {
        $(".jp-playlist").css("visibility", "hidden");
    });
    $("a.jp-restore-screen").on('click', function() {
        $(".jp-playlist").css("visibility", "visible");
    });
<?php
}
?>
<?php
    if (AmpConfig::get('waveform')) {
?>
    $("#jquery_jplayer_1").bind($.jPlayer.event.timeupdate, function (event) {
        if (event.jPlayer.status.duration > 0) {
            var leftpos = 400 * (event.jPlayer.status.currentTime / event.jPlayer.status.duration);
            $(".waveform-time").css({left: leftpos});
        }
    });
<?php } ?>
    $("#jquery_jplayer_1").bind($.jPlayer.event.volumechange, function(event) {
        $.cookie('jp_volume', event.jPlayer.options.volume, { expires: 7, path: '/'});
    });

<?php echo WebPlayer::add_media_js($playlist); ?>
});
<?php
    if (AmpConfig::get('waveform')) {
?>
function clickTimeOffset(e)
{
    var parrentOffset = $(".waveform").offset().left;
    var offset = e.pageX - parrentOffset;
    var duration = $("#jquery_jplayer_1").data("jPlayer").status.duration;
    var time = duration * (offset / 400);

    return time;
}

function ShowWaveform()
{
    $('.waveform').css('visibility', 'visible');
}

function HideWaveform()
{
    $('.waveform').css('visibility', 'hidden');
}
<?php } ?>
</script>
</head>
<body>
<?php
if ($iframed) {
?>
  <div class="jp-close">
    <a href="javascript:ExitPlayer();" title="Close Player"><img src="images/close.png" border="0" /></a>
  </div>
<?php
}
?>
<?php
if (!$isVideo) {
    $playerClass = "jp-audio";
?>
<div class="playing_info">
    <div class="playing_artist"></div>
    <div class="playing_title"></div>
    <div class="playing_lyrics"></div>
    <div class="playing_actions"></div>
</div>
<?php
} else {
    $playerClass = "jp-video jp-video-270p";
} ?>
<div class="jp-area <?php if(!AmpConfig::get('waveform')) echo "jp-area-center"; ?>">
  <div id="jp_container_1" class="<?php echo $playerClass; ?>">
    <div class="jp-type-playlist">
      <div id="jquery_jplayer_1" class="jp-jplayer"></div>
      <div class="jp-gui">
<?php
if ($isVideo) {
?>
        <div class="jp-video-play">
            <a href="javascript:;" class="jp-video-play-icon" tabindex="1">play</a>
        </div>
<?php } ?>
        <div class="jp-interface">
<?php
if ($isVideo) {
?>
            <div class="jp-progress">
                <div class="jp-seek-bar">
                    <div class="jp-play-bar"></div>
                </div>
            </div>
            <div class="jp-current-time"></div>
            <div class="jp-duration"></div>
            <div class="jp-title"></div>
            <div class="jp-controls-holder">
                <ul class="jp-controls">
                    <li><a href="javascript:;" class="jp-previous" tabindex="1">previous</a></li>
                    <li><a href="javascript:;" class="jp-play" tabindex="1">play</a></li>
                    <li><a href="javascript:;" class="jp-pause" tabindex="1">pause</a></li>
                    <li><a href="javascript:;" class="jp-next" tabindex="1">next</a></li>
                    <li><a href="javascript:;" class="jp-stop" tabindex="1">stop</a></li>
                    <li><a href="javascript:;" class="jp-mute" tabindex="1" title="mute">mute</a></li>
                    <li><a href="javascript:;" class="jp-unmute" tabindex="1" title="unmute">unmute</a></li>
                    <li><a href="javascript:;" class="jp-volume-max" tabindex="1" title="max volume">max volume</a></li>
                </ul>
                <div class="jp-volume-bar">
                    <div class="jp-volume-bar-value"></div>
                </div>

                <ul class="jp-toggles">
                    <li><a href="javascript:;" class="jp-full-screen" tabindex="1" title="full screen">full screen</a></li>
                    <li><a href="javascript:;" class="jp-restore-screen" tabindex="1" title="restore screen">restore screen</a></li>
                    <li><a href="javascript:;" class="jp-shuffle" tabindex="1" title="shuffle">shuffle</a></li>
                    <li><a href="javascript:;" class="jp-shuffle-off" tabindex="1" title="shuffle off">shuffle off</a></li>
                    <li><a href="javascript:;" class="jp-repeat" tabindex="1" title="repeat">repeat</a></li>
                    <li><a href="javascript:;" class="jp-repeat-off" tabindex="1" title="repeat off">repeat off</a></li>
                </ul>
            </div>
<?php } else { ?>
            <ul class="jp-controls">
              <li><a href="javascript:;" class="jp-previous" tabindex="1">previous</a></li>
              <li><a href="javascript:;" class="jp-play" tabindex="1">play</a></li>
              <li><a href="javascript:;" class="jp-pause" tabindex="1">pause</a></li>
              <li><a href="javascript:;" class="jp-next" tabindex="1">next</a></li>
              <li><a href="javascript:;" class="jp-stop" tabindex="1">stop</a></li>
              <li><a href="javascript:;" class="jp-mute" tabindex="1" title="mute">mute</a></li>
              <li><a href="javascript:;" class="jp-unmute" tabindex="1" title="unmute">unmute</a></li>
              <li><a href="javascript:;" class="jp-volume-max" tabindex="1" title="max volume">max volume</a></li>
            </ul>
            <div class="jp-progress">
              <div class="jp-seek-bar">
                <div class="jp-play-bar"></div>
              </div>
            </div>
            <div id="jquery_jplayer_1_volume_bar" class="jp-volume-bar">
              <div id="jquery_jplayer_1_volume_bar_value" class="jp-volume-bar-value"></div>
            </div>
            <div class="jp-current-time"></div>
            <div class="jp-duration"></div>
            <ul class="jp-toggles">
                <li><a href="javascript:;" class="jp-shuffle" tabindex="1" title="shuffle">shuffle</a></li>
                <li><a href="javascript:;" class="jp-shuffle-off" tabindex="1" title="shuffle off">shuffle off</a></li>
                <li><a href="javascript:;" class="jp-repeat" tabindex="1" title="repeat">repeat</a></li>
                <li><a href="javascript:;" class="jp-repeat-off" tabindex="1" title="repeat off">repeat off</a></li>
            </ul>
<?php if (AmpConfig::get('waveform')) { ?>
            <div class="waveform"></div>
<?php } ?>
<?php } ?>
        </div>
      </div>
      <div class="jp-playlist" style="position: absolute;">
          <ul>
              <li></li>
          </ul>
      </div>
      <div class="jp-no-solution">
        <span>Unsupported</span>
        This media is not supported by the player. Is your browser up to date?
      </div>
    </div>
  </div>
</div>
</body>
</html>