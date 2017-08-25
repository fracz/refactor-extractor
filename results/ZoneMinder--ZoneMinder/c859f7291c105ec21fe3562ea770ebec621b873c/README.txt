commit c859f7291c105ec21fe3562ea770ebec621b873c
Author: Isaac Connor <iconnor@connortechnology.com>
Date:   Mon May 15 22:02:48 2017 -0400

    Feature h264 videostorage (#1882)

    * Moved writing of configure options from Controller to Model.  Fixes #191.

    * Initial commit for saving events as videos :)

    * Add zm_video.cpp to autotools

    * Add zm_video.h to autotools

    * Search for MP4V2 header file 3 times: mp4v2/mp4v2.h, mp4v2.h, mp4.h

    * Fix serve memory leak

    * Few minor code improvements

    * Added the ability to override preset, tune, profile and few other improvements

    * Correctly write SPS & PPS from x264 encoder headers

    * Remove unnessecary SPS & PPS writing code

    * Imported missing files from master to feature-h264-videostorage

    * Audio support including fixes for dts/pts, split on keyframe and update to mkv extension to prevent ffmpeg problems writing rtsp audio to mp4 containter (header problem)

    * Updates to make gcc happy

    * Add html5 video control to timeline and event to support mkv playback

    * Add zm_videostore.cpp to CMakeLists.txt

    * Remove Modern Branch for now

    * Fix minor bug

    * Option handled added in master, removing duplicate declaration

    * Add CaptureandRecord from zm_camera.h

    * Putting placeholder in for CaptureAndRecord function

    * Removed duplicate code and brackets

    * add digest auth file for cmake

    Conflicts:
            src/CMakeLists.txt

    * Add web dir back into Makefile.am
    Revert "Removed web from SUBDIRS in Makefile.am"

    This reverts commit d9bbcdf3a94cba4d8955fcd03bd965ed2772f34d.

    * Add CaptureAndRecord to vlc, still need to make it record

    * Resolve SegFault on videostore

    * Swap to mp4 container

    * mp4 changes

    * spaces to tabs, hide video stuff if video writer is turned off

    * Make timeline open event.mp4 instead of mkv

    * Missed mkv in timeline.js

    * Fix some issues from the merge conflict

    * Resolve post merge build issues with braces

    * Fix whitespace

    * Update Jpeg and Video options for passthrough options

    * Whitespace fix zm_camera.h

    * Fix array mkssing comma

    * Add support for Jpeg save options for h264 branch snapshot. Might remove altogether if snapshots not needed

    * Update VideoStoreData memory size comment

    * Change from config.use_mkv_storage to per monitor option VideoWriter from video branch

    * Fix bracket issues post merge

    * Clean up comments and add av_free_packet

    * Convert from event_directory to event file as per Video branch

    * Testing videojs for video playback

    * Fixed a missing bracket post merge and also SQL_values now used for EventID and Monitors

    * bring recent improvements in ffmpeg capture function into captureandrecord

    * Remove pict from writeAudioFramePacket as not used

    * Add translate options for h264 Storage options in Monitor and update en_gb file

    * Cherry-Pick from iconnor - make it compile on ubuntu 15.04.  Which is libav 56.1.0

    Conflicts:
            src/zm_ffmpeg.cpp
            src/zm_remote_camera_rtsp.cpp

    Conflicts:
            distros/ubuntu1204/changelog

    * Clean up videostore code and remove lots of unused code

    * proof of concept for dynamic/automatic video rotation using video-js plugin zoomrotate

    Conflicts:
            web/skins/classic/views/event.php

    * removed redundant field in sql query

    Conflicts:
            web/skins/classic/views/event.php

    * local storage of video js plugin

    * Beautify!

    Make the code somewhat readable.

    * added missing videojs.zoomrotate.js file

    added missing videojs.zoomrotate.js file

    * Typo

    added missing "

    * Added missing brackets

    * fix to display thumbnails when only storing snapshot.jpg

    * added control for video playback rate

    Conflicts:
            web/skins/classic/views/event.php

    * dynamically create jpegs from video file for viewing in browser

    * fix timeline view for SaveJPEGs monitors (without enabled VideoWriter)

    * only expose monitor info which are being used in client

    * fix segmentation fault in zma with ubuntu 14.04 and ffmpeg 2.5.8 (gcc 4.8)

    when libx264 is not installed

    * better way of detecting showing image or video in timeline and event view

    instead of Monitor.VideoWriter, Event.DefaultVideo is used, so even if
    VideoWriter/SaveJPEG option is changed, a valid image or video will always be
    displayed for historical events in both timeline and event view

    this also fixes loading videos in timeline view

    * Fixes problem of crashing zmc when bad packet arrives causing av_interleaved_write_frame() to return non-zero (-22).  Prefilters common packet issues. Add metadata title to generated video file

    * Remove syslog.h

    * fixed SaveJPEGs are not working

    which is caused in errors introduced when merging with master

    * Update README.md

    * Fix build warnings specific to h264 branch, unused FrameImg, unused ret and int64_t snprintf issues

    * Fix PRId64 issue in travis, builds locally fine, but I can see a gcc version issue here

    * Fix PRId64 issue in travis, another try

    * Try "STDC_FORMAT_MACROS" to see if that helps Travis on gcc 4.6.3

    * Revert space removal around PRId64

    * video branch ffmpeg 2.9 fixes

    ffmpeg 2.9 patched removed SSE2 CPU

    * Add FFMPEGInit back

    * use webvvt to overlay timestamp (honoring Monitor.LabelFormat) to videos in timeline and event

    also fixed bug which prevented seeking in timeline video preview

    * ffmpeg 3.0 API build failure fixes

    * Update README.md

    * merge all the commits from the messed up iconnor_video branch

    * fix whitespace

    * revert

    * whitespace fixes

    * spelling fix

    * put back some text

    * add these back

    * fix spelling mistake

    * Steal some packet dumping routines from ffmpeg. Convert them to use our logging routines

    * add a test and error message if the codec is not h264

    * these have been removed in master

    * add a view to check auth and just send the video

    * add some comments, and dump filename and AVFormatContext on failure to write header

    * add the toggle for RecordAudio so that the checkbox works to turn off Audio

    * Must init videoStore in constuctor

    * more debug and comments, return checking

    * Fix dropped part of sql query.

    * fix extra else and some whitespace

    * Fix missing } from merge that was preventing building.

    * fix tabs

    * get rid of use of separator, just use \n

    * Restore lost fixes for deprecation

    * Why are these failing

    * Respect record_audio flag when setting up video file so dont try and initiliase mp4 with unsupported audio

    * Forgot that I was trying to solve case of stream is true and record_audio
    is false.

    * Pass swscale_ctx back in to getCachedContext or it will create new
    context every frame and leak memory like a mofo.

    * Add libx264-dev and libmp4v2-dev to build requires to save hassle of
    ensuring they are installed before build.

    * Merge my Rotation/Orientation work and fixes for bad h264 streams

    * need arpa/inet for reverse lookups

    * pull in the new byte range code for viewing videos

    * Move our recording flag deeper into closeevent

    * add braces and only call closeEvent if there is an event

    * deprecate the z_frame_rate stuff which is deprecated in ffmpeg

    * remark out some debugging

    * fix for video on stream 1

    * fix audio_stream to audio_st

    * Ignore bad decodes

    * fix problems with content-length causing viewing to not work in chrome/android

    * change logic of sending file contents to handle an off by one and be more readable

    * Some fixes pointed out by Maxim Romanov.  Also simply the loading of events to not join the Monitors table

    * fix to sql for timeline

    * added RecordAudio to sql in README

    * Use sub queries instead of joins to fix errors when using new mysql defaults.

    * fix sql queries

    * Dockerfile to build feature-h264-videostorage

    * Must cast codec

    * add php-acpu as a dependency

    * require php5-acpu

    * fix typo

    * remove extra /

    * Add a line for out-of-tree builds to do api/lib/Cake/bootstrap.php

    * delete merge conflict files

    * delete merge conflict files