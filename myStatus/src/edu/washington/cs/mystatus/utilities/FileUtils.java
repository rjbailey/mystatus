/*
 * Copyright (C) 2009 University of Washington
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

package edu.washington.cs.mystatus.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import edu.washington.cs.mystatus.application.MyStatus;
import edu.washington.cs.mystatus.logic.FormController;
import edu.washington.cs.mystatus.logic.HierarchyElement;

/**
 * Static methods used for common file operations.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FileUtils {
    private final static String t = "FileUtils";

    // Used to validate and display valid form names.
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

    public static final String FORMID = "formid";
    public static final String VERSION = "version"; // arbitrary string in OpenRosa 1.0
    public static final String TITLE = "title";
    public static final String SUBMISSIONURI = "submission";
    public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";
    public static final String PREDICATE = "predicate";
    public static final String TRIGGER = "trigger";

    
    public static boolean createFolder(String path) {
        boolean made = true;
        File dir = new File(path);
        if (!dir.exists()) {
            made = dir.mkdirs();
        }
        return made;
    }


    public static byte[] getFileAsBytes(File file) {
        byte[] bytes = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                Log.e(t, "File " + file.getName() + "is too large");
                return null;
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int read = 0;
            try {
                while (offset < bytes.length && read >= 0) {
                    read = is.read(bytes, offset, bytes.length - offset);
                    offset += read;
                }
            } catch (IOException e) {
                Log.e(t, "Cannot read " + file.getName());
                e.printStackTrace();
                return null;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                try {
                    throw new IOException("Could not completely read file " + file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return bytes;

        } catch (FileNotFoundException e) {
            Log.e(t, "Cannot find " + file.getName());
            e.printStackTrace();
            return null;

        } finally {
            // Close the input stream
            try {
                is.close();
            } catch (IOException e) {
                Log.e(t, "Cannot close input stream for " + file.getName());
                e.printStackTrace();
                return null;
            }
        }
    }


    public static String getMd5Hash(File file) {
        try {
            // CTS (6/15/2010) : stream file through digest instead of handing it the byte[]
            MessageDigest md = MessageDigest.getInstance("MD5");
            int chunkSize = 256;

            byte[] chunk = new byte[chunkSize];

            // Get the size of the file
            long lLength = file.length();

            if (lLength > Integer.MAX_VALUE) {
                Log.e(t, "File " + file.getName() + "is too large");
                return null;
            }

            int length = (int) lLength;

            InputStream is = null;
            is = new FileInputStream(file);

            int l = 0;
            for (l = 0; l + chunkSize < length; l += chunkSize) {
                is.read(chunk, 0, chunkSize);
                md.update(chunk, 0, chunkSize);
            }

            int remaining = length - l;
            if (remaining > 0) {
                is.read(chunk, 0, remaining);
                md.update(chunk, 0, remaining);
            }
            byte[] messageDigest = md.digest();

            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);
            while (md5.length() < 32)
                md5 = "0" + md5;
            is.close();
            return md5;

        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getMessage());
            return null;

        } catch (FileNotFoundException e) {
            Log.e("No Cache File", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Problem reading from file", e.getMessage());
            return null;
        }

    }


    public static Bitmap getBitmapScaledToDisplay(File f, int screenHeight, int screenWidth) {
        // Determine image size of f
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);

        int heightScale = o.outHeight / screenHeight;
        int widthScale = o.outWidth / screenWidth;

        // Powers of 2 work faster, sometimes, according to the doc.
        // We're just doing closest size that still fills the screen.
        int scale = Math.max(widthScale, heightScale);

        // get bitmap with scale ( < 1 is the same as 1)
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inInputShareable = true;
        options.inPurgeable = true;
        options.inSampleSize = scale;
        Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        if (b != null) {
        Log.i(t,
            "Screen is " + screenHeight + "x" + screenWidth + ".  Image has been scaled down by "
                    + scale + " to " + b.getHeight() + "x" + b.getWidth());
        }
        return b;
    }


    public static void copyFile(File sourceFile, File destFile) {
        if (sourceFile.exists()) {
            FileChannel src;
            try {
                src = new FileInputStream(sourceFile).getChannel();
                FileChannel dst = new FileOutputStream(destFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            } catch (FileNotFoundException e) {
                Log.e(t, "FileNotFoundExeception while copying audio");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(t, "IOExeception while copying audio");
                e.printStackTrace();
            }
        } else {
            Log.e(t, "Source file does not exist: " + sourceFile.getAbsolutePath());
        }

    }
    
    public static HashMap<String, String> parseXML(File xmlFile) {
        HashMap<String, String> fields = new HashMap<String, String>();
        InputStream is;
        try {
            is = new FileInputStream(xmlFile);
        } catch (FileNotFoundException e1) {
            throw new IllegalStateException(e1);
        }

        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.w(t, "UTF 8 encoding unavailable, trying default encoding");
            isr = new InputStreamReader(is);
        }

        if (isr != null) {

            Document doc;
            try {
                doc = XFormParser.getXMLDocument(isr);
            } finally {
                try {
                    isr.close();
                } catch (IOException e) {
                    Log.w(t, xmlFile.getAbsolutePath() + " Error closing form reader");
                    e.printStackTrace();
                }
            }

            String xforms = "http://www.w3.org/2002/xforms";
            String html = doc.getRootElement().getNamespace();
            
            Element head = doc.getRootElement().getElement(html, "head");
            Element title = head.getElement(html, "title");
            if (title != null) {
                fields.put(TITLE, XFormParser.getXMLText(title, true));
            } 
            
            Element model = getChildElement(head, "model");
            Element cur = getChildElement(model,"instance");
            
            int idx = cur.getChildCount();
            int i;
            for (i = 0; i < idx; ++i) {
                if (cur.isText(i))
                    continue;
                if (cur.getType(i) == Node.ELEMENT) {
                    break;
                }
            }

            if (i < idx) {
                cur = cur.getElement(i); // this is the first data element
                String id = cur.getAttributeValue(null, "id");
                String xmlns = cur.getNamespace();
                
                String version = cur.getAttributeValue(null, "version");
                String uiVersion = cur.getAttributeValue(null, "uiVersion");
                if ( uiVersion != null ) {
                	// pre-OpenRosa 1.0 variant of spec
                	Log.e(t, "Obsolete use of uiVersion -- IGNORED -- only using version: " + version);
                }

                fields.put(FORMID, (id == null) ? xmlns : id);
                fields.put(VERSION, (version == null) ? null : version);
            } else {
                throw new IllegalStateException(xmlFile.getAbsolutePath() + " could not be parsed");
            }
            try {
                Element submission = model.getElement(xforms, "submission");
                String submissionUri = submission.getAttributeValue(null, "action");
                fields.put(SUBMISSIONURI, (submissionUri == null) ? null : submissionUri);
                String base64RsaPublicKey = submission.getAttributeValue(null, "base64RsaPublicKey");
                fields.put(BASE64_RSA_PUBLIC_KEY,
                  (base64RsaPublicKey == null || base64RsaPublicKey.trim().length() == 0) 
                  ? null : base64RsaPublicKey.trim());
            } catch (Exception e) {
                Log.i(t, xmlFile.getAbsolutePath() + " does not have a submission element");
                // and that's totally fine.
            }
            try {
                String predicate = model.getAttributeValue(null, "predicate");
                if (!TextUtils.isEmpty(predicate)) {
                    fields.put(PREDICATE, predicate);
                }
            } catch (Exception e) {
                Log.i(t, xmlFile.getAbsolutePath() + " does not have a predicate attribute");
            }
            try {
                String trigger = model.getAttributeValue(null, "trigger");
                if (!TextUtils.isEmpty(trigger)) {
                    fields.put(TRIGGER, trigger);
                }
            } catch (Exception e) {
                Log.i(t, xmlFile.getAbsolutePath() + " does not have a trigger attribute");
            }

        }
        return fields;
    }
    // needed because element.getelement fails when there are attributes
    private static Element getChildElement(Element parent, String childName) {
        Element e = null;
        int c = parent.getChildCount();
        int i = 0;
        for (i = 0; i < c; i++) {
            if (parent.getType(i) == Node.ELEMENT) {
                if (parent.getElement(i).getName().equalsIgnoreCase(childName)) {
                    return parent.getElement(i);
                }
            }
        }
        return e;
    }
    
    
    /**
     * Used for clean up all files in temp path directory
     * @param directory
     * @CD: borrowed from ODK file utils
     */
    public static void deleteAllFilesInDirectoryRecursively(File directory) {
        if (directory.exists()) {
            if (directory.isDirectory()) {
                // delete all the files in the directory
                File[] files = directory.listFiles();
                for (File f : files) {
                	// adding recursive calls
                    if (f.isDirectory()){
                    	deleteAllFilesInDirectoryRecursively(f);
                    }
                    f.delete();
                }
            }
            directory.delete();
        }
    }
    
    
    // get the List of Hierarchy Elements
    public static ArrayList<HierarchyElement> getHierarchyElements(){
        FormController formController = MyStatus.getInstance().getFormController();
        // Record the current index so we can return to the same place if the user hits 'back'.
        FormIndex currentIndex = formController.getFormIndex();

        // If we're not at the first level, we're inside a repeated group so we want to only display
        // everything enclosed within that group.
        String enclosingGroupRef = "";
        ArrayList<HierarchyElement>formList = new ArrayList<HierarchyElement>();

        // If we're currently at a repeat node, record the name of the node and step to the next
        // node to display.
        if (formController.getEvent() == FormEntryController.EVENT_REPEAT) {
            enclosingGroupRef =
                    formController.getFormIndex().getReference().toString(false);
            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
        } else {
            FormIndex startTest = formController.stepIndexOut(currentIndex);
            // If we have a 'group' tag, we want to step back until we hit a repeat or the
            // beginning.
            while (startTest != null
                    && formController.getEvent(startTest) == FormEntryController.EVENT_GROUP) {
                startTest = formController.stepIndexOut(startTest);
            }
            if (startTest == null) {
                // check to see if the question is at the first level of the hierarchy. If it is,
                // display the root level from the beginning.
                formController.jumpToIndex(FormIndex
                        .createBeginningOfFormIndex());
            } else {
                // otherwise we're at a repeated group
                formController.jumpToIndex(startTest);
            }

            // now test again for repeat. This should be true at this point or we're at the
            // beginning
            if (formController.getEvent() == FormEntryController.EVENT_REPEAT) {
                enclosingGroupRef =
                        formController.getFormIndex().getReference().toString(false);
                formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            }
        }

        int event = formController.getEvent();
        if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
            // The beginning of form has no valid prompt to display.
            // formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            //mPath.setVisibility(View.GONE);
            //jumpPreviousButton.setEnabled(false);
        } else {
            //mPath.setVisibility(View.VISIBLE);
            //mPath.setText(getCurrentPath());
            //jumpPreviousButton.setEnabled(true);
        }

        // Refresh the current event in case we did step forward.
        event = formController.getEvent();

        // There may be repeating Groups at this level of the hierarchy, we use this variable to
        // keep track of them.
        String repeatedGroupRef = "";

        event_search: while (event != FormEntryController.EVENT_END_OF_FORM) {
            switch (event) {
            case FormEntryController.EVENT_QUESTION:
                if (!repeatedGroupRef.equalsIgnoreCase("")) {
                    // We're in a repeating group, so skip this question and move to the next
                    // index.
                    event =
                            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                    continue;
                }

                FormEntryPrompt fp = formController.getQuestionPrompt();
                String label = fp.getLongText();
                if ( !fp.isReadOnly() || (label != null && label.length() > 0) ) {
                    // show the question if it is an editable field.
                    // or if it is read-only and the label is not blank.
                    formList.add(new HierarchyElement(fp.getLongText(), fp.getAnswerText(), null,
                            Color.WHITE,4 , fp.getIndex()));
                }
                break;
            case FormEntryController.EVENT_GROUP:
                // ignore group events
                break;
            case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                if (enclosingGroupRef.compareTo(formController
                        .getFormIndex().getReference().toString(false)) == 0) {
                    // We were displaying a set of questions inside of a repeated group. This is
                    // the end of that group.
                    break event_search;
                }

                if (repeatedGroupRef.compareTo(formController.getFormIndex()
                        .getReference().toString(false)) != 0) {
                    // We're in a repeating group, so skip this repeat prompt and move to the
                    // next event.
                    event =
                            formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                    continue;
                }

                if (repeatedGroupRef.compareTo(formController.getFormIndex()
                        .getReference().toString(false)) == 0) {
                    // This is the end of the current repeating group, so we reset the
                    // repeatedGroupName variable
                    repeatedGroupRef = "";
                }
                break;
            case FormEntryController.EVENT_REPEAT:
                FormEntryCaption fc = formController.getCaptionPrompt();
                if (enclosingGroupRef.compareTo(formController
                        .getFormIndex().getReference().toString(false)) == 0) {
                    // We were displaying a set of questions inside a repeated group. This is
                    // the end of that group.
                    break event_search;
                }
                if (repeatedGroupRef.equalsIgnoreCase("") && fc.getMultiplicity() == 0) {
                    // This is the start of a repeating group. We only want to display
                    // "Group #", so we mark this as the beginning and skip all of its children
                    HierarchyElement group =
                            new HierarchyElement(fc.getLongText(), null, null, Color.WHITE,
                                    3, fc.getIndex());
                    repeatedGroupRef =
                            formController.getFormIndex().getReference()
                            .toString(false);
                    formList.add(group);
                }

                if (repeatedGroupRef.compareTo(formController.getFormIndex()
                        .getReference().toString(false)) == 0) {
                    // Add this group name to the drop down list for this repeating group.
                    HierarchyElement h = formList.get(formList.size() - 1);
                    h.addChild(new HierarchyElement("     " + fc.getLongText() + " "
                            + (fc.getMultiplicity() + 1), null, null, Color.WHITE, 1, fc
                            .getIndex()));
                }
                break;
            }
            event =
                    formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
        }


        formController.jumpToIndex(currentIndex);
        return formList;
    }
}
