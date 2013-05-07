/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.washington.cs.mystatus.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import edu.washington.cs.mystatus.R;

import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.utilities.DataEncryptionUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * @author ctsims
 * @author carlhartung
 */
public class AudioButton extends ImageButton implements OnClickListener {
    private final static String t = "AudioButton";

    /**
     * Useful class for handling the playing and stopping of audio prompts.
     * This is used here, and also in the GridMultiWidget and GridWidget
     * to play prompts as items are selected.
     * 
     * @author mitchellsundt@gmail.com
     *
     */
    public static class AudioHandler {
        private FormIndex index;
        private String selectionDesignator;
        private String URI;
        private MediaPlayer player;

        public AudioHandler(FormIndex index, String selectionDesignator, String URI) {
            this.index = index;
            this.selectionDesignator = selectionDesignator;
            this.URI = URI;
            player = null;
        }
        public void playAudio(Context c) {
        	MyStatus.getInstance().getActivityLogger().logInstanceAction(this, "onClick.playAudioPrompt", selectionDesignator, index);
            if (URI == null) {
                // No audio file specified
                Log.e(t, "No audio file was specified");
                Toast.makeText(c, c.getString(R.string.audio_file_error),
                    Toast.LENGTH_LONG).show();
                return;
            }

            String audioFilename = "";
            try {
                audioFilename = ReferenceManager._().DeriveReference(URI).getLocalURI();
            } catch (InvalidReferenceException e) {
                Log.e(t, "Invalid reference exception");
                e.printStackTrace();
            }

            File audioFile = new File(audioFilename);
            if (!audioFile.exists()) {
                // We should have an audio clip, but the file doesn't exist.
                String errorMsg = c.getString(R.string.file_missing, audioFile);
                Log.e(t, errorMsg);
                Toast.makeText(c, errorMsg, Toast.LENGTH_LONG).show();
                return;
            }

            // In case we're currently playing sounds.
            stopPlaying();
            
            // need to decrypt the audio file first 
            // @CD
            String tempPathFile = MyStatus.TEMP_MEDIA_PATH + audioFilename.substring(audioFilename.lastIndexOf("/"))+"temp"
					+audioFilename.substring(audioFilename.lastIndexOf("."));
            System.out.println(tempPathFile);
            // check if the file is already exist or not
            // add some number to make the file unique
            // @CD
            File testFile = new File (tempPathFile);
            int counter = 0;
            while (testFile.exists()){
            	tempPathFile = tempPathFile.substring(0, tempPathFile.lastIndexOf(".")) 
            					+ counter 
            					+ tempPathFile.substring(tempPathFile.lastIndexOf("."));
            	counter++;
            }
            
            
            final File tf = new File (tempPathFile);
            try {
				FileOutputStream fos = new FileOutputStream(tf);
				FileInputStream fis = new FileInputStream(audioFilename);
				DataEncryptionUtils dataDecryption = new DataEncryptionUtils();
				dataDecryption.InitCiphers();
				dataDecryption.CBCDecrypt(fis, fos);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}catch (Exception e){
				e.printStackTrace();
			}
            
            
            
            player = new MediaPlayer();
            try {
                //player.setDataSource(audioFilename);
                player.setDataSource(tempPathFile);
                player.prepare();
                player.start();
                player.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                        // delete the tempFile
                       // @CD
                        // not needed will delete it at clean up
                       // tf.delete();
                    }

                });
            } catch (IOException e) {
                String errorMsg = c.getString(R.string.audio_file_invalid);
                Log.e(t, errorMsg);
                Toast.makeText(c, errorMsg, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        	
        }

        public void stopPlaying() {
            if (player != null) {
                player.release();
            }
        }
    }

    AudioHandler handler; 
    
    public AudioButton(Context context, FormIndex index, String selectionDesignator, String URI) {
        super(context);
        this.setOnClickListener(this);
        handler = new AudioHandler( index, selectionDesignator, URI);
        Bitmap b =
            BitmapFactory.decodeResource(context.getResources(),
                android.R.drawable.ic_lock_silent_mode_off);
        this.setImageBitmap(b);
    }

    public void playAudio() {
    	handler.playAudio(getContext());
    }

    @Override
    public void onClick(View v) {
    	playAudio();
    }


    public void stopPlaying() {
        handler.stopPlaying();
    }
}
