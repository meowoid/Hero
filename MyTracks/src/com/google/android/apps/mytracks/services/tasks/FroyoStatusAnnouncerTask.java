/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.mytracks.services.tasks;

import static com.google.android.apps.mytracks.Constants.TAG;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

import java.util.HashMap;


/**
 * This class will periodically announce the user's trip statistics for Froyo and future handsets.
 * This class will request and release audio focus.
 *
 * @author Sandor Dornbush
 */
public class FroyoStatusAnnouncerTask extends StatusAnnouncerTask {
  private final static HashMap<String, String> SPEECH_PARAMS = new HashMap<String, String>();
  static {
    SPEECH_PARAMS.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "not_used");
  }

  private final AudioManager audioManager;
  private final OnUtteranceCompletedListener utteranceListener =
      new OnUtteranceCompletedListener() {
        @Override
        public void onUtteranceCompleted(String utteranceId) {
          int result = audioManager.abandonAudioFocus(null);
          if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Log.w(TAG, "FroyoStatusAnnouncerTask: Failed to relinquish audio focus");
          }
        }
      };

  public FroyoStatusAnnouncerTask(Context context) {
    super(context);

    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
  }

  @Override
  protected void onTtsReady() {
    super.onTtsReady();

    tts.setOnUtteranceCompletedListener(utteranceListener);
  }

  @Override
  protected synchronized void speakAnnouncement(String announcement) {
    int result = audioManager.requestAudioFocus(null,
          TextToSpeech.Engine.DEFAULT_STREAM,
          AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
      Log.w(TAG, "FroyoStatusAnnouncerTask: Request for audio focus failed.");
    }

    // We don't care about the utterance id.
    // It is supplied here to force onUtteranceCompleted to be called.
    tts.speak(announcement, TextToSpeech.QUEUE_FLUSH, SPEECH_PARAMS);
  }
}
