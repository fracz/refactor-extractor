/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ParsedReaderResult;
import com.google.zxing.client.result.ParsedReaderResultType;

/**
 * The barcode reader activity itself. This is loosely based on the CameraPreview
 * example included in the Android SDK.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Android Team (for CameraPreview example)
 */
public final class BarcodeReaderCaptureActivity extends Activity {

  private CameraManager cameraManager;
  private CameraSurfaceView surfaceView;
  private WorkerThread workerThread;

  private static final int ABOUT_ID = Menu.FIRST;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    cameraManager = new CameraManager(getApplication());
    surfaceView = new CameraSurfaceView(getApplication(), cameraManager);
    setContentView(surfaceView);
    workerThread = new WorkerThread(surfaceView, cameraManager, messageHandler);
    workerThread.requestPreviewLoop();
    workerThread.start();

    // TODO re-enable this when issues with Matrix.setPolyToPoly() are resolved
    //GridSampler.setGridSampler(new AndroidGraphicsGridSampler());
  }

  @Override
  protected void onResume() {
    super.onResume();
    cameraManager.openDriver();
    if (workerThread == null) {
      workerThread = new WorkerThread(surfaceView, cameraManager, messageHandler);
      workerThread.requestPreviewLoop();
      workerThread.start();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (workerThread != null) {
      workerThread.requestExitAndWait();
      workerThread = null;
    }
    cameraManager.closeDriver();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
      workerThread.requestStillAndDecode();
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, ABOUT_ID, R.string.menu_about);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(Menu.Item item) {
    switch (item.getId()) {
      case ABOUT_ID:
        Context context = getApplication();
        showAlert(
          context.getString(R.string.title_about),
          0,
          context.getString(R.string.msg_about),
          context.getString(R.string.button_ok),
          true);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private final Handler messageHandler = new Handler() {
    @Override
    public void handleMessage(Message message) {
      switch (message.what) {
        case R.id.decoding_succeeded_message:
          handleDecode((Result) message.obj);
          break;
        case R.id.decoding_failed_message:
          Context context = getApplication();
          showAlert(
            context.getString(R.string.title_no_barcode_detected),
            0,
            context.getString(R.string.msg_no_barcode_detected),
            context.getString(R.string.button_ok),
            true);
          break;
      }
    }
  };

  public void restartPreview() {
    workerThread.requestPreviewLoop();
  }

  private void handleDecode(Result rawResult) {
    ResultPoint[] points = rawResult.getResultPoints();
    if (points != null && points.length > 0) {
      surfaceView.drawResultPoints(points);
    }

    Context context = getApplication();
    ParsedReaderResult readerResult = parseReaderResult(rawResult);
    ResultHandler handler = new ResultHandler(this, readerResult);
    if (handler.getIntent() != null) {
      // Can be handled by some external app; ask if the user wants to
      // proceed first though
      showAlert(
        context.getString(getDialogTitleID(readerResult.getType())),
        0,
        readerResult.getDisplayResult(),
        context.getString(R.string.button_yes),
        handler,
        context.getString(R.string.button_no),
        null,
        true,
        null
      );

    } else {
      // Just show information to user
      showAlert(
        context.getString(R.string.title_barcode_detected),
        0,
        readerResult.getDisplayResult(),
        context.getString(R.string.button_ok),
        true);
    }
  }

  private static ParsedReaderResult parseReaderResult(Result rawResult) {
    ParsedReaderResult readerResult = ParsedReaderResult.parseReaderResult(rawResult);
    if (readerResult.getType().equals(ParsedReaderResultType.TEXT)) {
      String rawText = rawResult.getText();
      AndroidIntentParsedResult androidResult = AndroidIntentParsedResult.parse(rawText);
      if (androidResult != null) {
        Intent intent = androidResult.getIntent();
        if (!Intent.VIEW_ACTION.equals(intent.getAction())) {
          // For now, don't take anything that just parses as a View action. A lot
          // of things are accepted as a View action by default.
          readerResult = androidResult;
        }
      }
    }
    return readerResult;
  }

  private static int getDialogTitleID(ParsedReaderResultType type) {
    if (type.equals(ParsedReaderResultType.ADDRESSBOOK)) {
      return R.string.title_add_contact;
    } else if (type.equals(ParsedReaderResultType.URI) ||
               type.equals(ParsedReaderResultType.BOOKMARK) ||
               type.equals(ParsedReaderResultType.URLTO)) {
      return R.string.title_open_url;
    } else if (type.equals(ParsedReaderResultType.EMAIL) ||
               type.equals(ParsedReaderResultType.EMAIL_ADDRESS)) {
      return R.string.title_compose_email;
    } else if (type.equals(ParsedReaderResultType.UPC)) {
      return R.string.title_lookup_barcode;
    } else if (type.equals(ParsedReaderResultType.TEL)) {
      return R.string.title_dial;
    } else if (type.equals(ParsedReaderResultType.GEO)) {
      return R.string.title_view_maps;
    } else {
      return R.string.title_barcode_detected;
    }
  }

}