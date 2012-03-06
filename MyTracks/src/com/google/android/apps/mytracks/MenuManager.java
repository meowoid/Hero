/*
 * Copyright 2010 Google Inc.
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
package com.google.android.apps.mytracks;

import com.google.android.maps.mytracks.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Manage the application menus.
 *
 * @author Sandor Dornbush
 */
class MenuManager {

  private final MyTracks activity;

  public MenuManager(MyTracks activity) {
    this.activity = activity;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    activity.getMenuInflater().inflate(R.menu.main, menu);

    // TODO: Replace search button with search widget if API level >= 11
    return true;
  }

  public void onPrepareOptionsMenu(Menu menu, boolean hasRecorded,
      boolean isRecording, boolean hasSelectedTrack) {
    menu.findItem(R.id.menu_markers)
        .setEnabled(hasRecorded && hasSelectedTrack);
    menu.findItem(R.id.menu_record_track)
        .setEnabled(!isRecording)
        .setVisible(!isRecording);
    menu.findItem(R.id.menu_stop_recording)
        .setEnabled(isRecording)
        .setVisible(isRecording);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_record_track: {
        activity.startRecording();
        return true;
      }
      case R.id.menu_stop_recording: {
        activity.stopRecording();
        return true;
      }
      case R.id.menu_tracks: {
	    activity.startActivityForResult(new Intent(activity, TrackList.class),
	    		Constants.SHOW_TRACK);
        return true;
      }
      case R.id.menu_markers: {
        Intent startIntent = new Intent(activity, WaypointsList.class);
        startIntent.putExtra("trackid", activity.getSelectedTrackId());
        activity.startActivityForResult(startIntent, Constants.SHOW_WAYPOINT);
        return true;
      }
      case R.id.menu_sensor_state: {
        return startActivity(SensorStateActivity.class);
      }
      case R.id.menu_settings: {
        return startActivity(SettingsActivity.class);
      }
      case R.id.menu_aggregated_statistics: {
        return startActivity(AggregatedStatsActivity.class);
      }
      case R.id.menu_help: {
        return startActivity(WelcomeActivity.class);
      }
      case R.id.menu_search: {
        // TODO: Pass the current track ID and current location to do some fancier ranking.
        activity.onSearchRequested();
        break;
      }
      case R.id.menu_dump_database: {
        try {
          backupDatabase(activity.getPackageName(), "mytracks.db");
        } catch (IOException e) {
          e.printStackTrace();
          Log.e("MyTracks", "Dump database file failed.");
        }
      }
    }
    return false;
  }

  private boolean startActivity(Class<? extends Activity> activityClass) {
    activity.startActivity(new Intent(activity, activityClass));
    return true;
  }
  
  private void backupDatabase(String pkgName, String fileName) throws IOException {
    File dbFile = new File("/data/data/" + pkgName + "/databases/" + fileName);
    if (dbFile.exists()) {
        FileInputStream fis = new FileInputStream(dbFile);
        String outFileName = Environment.getExternalStorageDirectory()+ "/" + fileName;
        OutputStream output = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.flush();
        output.close();
        fis.close();
    }
}
}
