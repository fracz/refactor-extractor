commit b16ec4ab754a05f18a7954b5cadb39d8edc8130b
Author: abrahambotros <abe@humandx.org>
Date:   Thu Nov 17 22:25:51 2016 -0800

    Minor Android MediaRecorder, sizing, and file improvements (#477)

    Android MediaRecorder:
      - Most importantly, call Camera.unlock() before setting the camera on the
        MediaRecorder instance, and release() not just reset() when releasing the MediaRecorder
        instance!
      - Add comments and notes for preparing and releasing MediaRecorder instance.
      - Add onError callback for errors during recording session.

    RCTCameraViewManager, RCTCamera, RCTCameraViewFinder, RCTCameraView:
      - Implement setCaptureMode, preparing camera based on captureMode. Currently, the only step that
        needs to be taken here is setting the recording hint for videos.
      - Handle setting _captureMode instance variable where applicable.

    Sizing
      - Determine ViewFinder supported sizes based on actual captureMode (i.e., get supported picture
        sizes when in still capture mode, and get supported video sizes when in video capture mode).

    Output files:
      - Get appropriate external storage public directory based on media type (image or video).
      - Minor variable renaming to indicate that both images or videos can be saved.

    README:
      - Update captureTarget to indicate that cameraRoll is the actual default for both systems.
      - Small clarification for output data type for deprecated memory captureTarget output.