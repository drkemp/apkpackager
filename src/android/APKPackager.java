// Copyright (c) 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.BufferedInputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import kellinwood.security.zipsigner.ZipSigner;
import android.util.Log;
import com.android.sdklib.build.*;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;


public class APKPackager  extends CordovaPlugin {

    private String LOG_TAG = "APKPackage";
  
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("package".equals(action)) {
            packageApk(args, callbackContext);
            return true;
        }
        return false;
    }

    private void packageApk(CordovaArgs args, CallbackContext callbackContext) {
    	String wwwdir="";
    	String resdir="";
    	String workdir="";
    	URL publicKeyUrl=null;
    	URL privateKeyUrl=null;
    	String keyPassword="";
    	
    	try {
	    	wwwdir = args.getString(0);
	        resdir = args.getString(1);
	        workdir= args.getString(2);
	        publicKeyUrl = new URL( args.getString(3));
	        privateKeyUrl= new URL( args.getString(3));
	        keyPassword= args.getString(5);
        } catch (Exception e) {
            callbackContext.error("Missing arguments: "+e.getMessage());        	
        }
        
        String generatedApkPath = workdir+"test.apk";
        String signedApkPath=workdir+"test-signed.apk";
        String resname =workdir+"res.zip";
        String assetsname =workdir+"assets.zip";
        String dexname = workdir+"classes.dex";
        String maniname = workdir+"AndroidManifest.xml";

        // merge the supplied www & res dirs into the dummy project
        // for this to work the relative path of the supplied dir must be the same as the desired path in the APK
        // ie. ./foo/bar.png with be at /foo/bar.png in the APK
        String tempres = workdir+"tempres";
        String tempassets = workdir+"tempasset";
        mergeDirectory(assetsname, wwwdir, tempassets);
        mergeDirectory(resname, resdir, tempres);

        // take the completed package and make the unsigned APK
        try{
            ApkBuilder b = new ApkBuilder(generatedApkPath,resname,dexname,null,null,null);
            b.addSourceFolder(new File(tempassets));
            b.addSourceFolder(new File(tempres));
            // now mangle all the XML
            // and add the binary XML and resource catalog
            
            b.addFile(new File(maniname),"AndroidManifest.xml");
            b.sealApk();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("ApkBuilder Error: "+e.getMessage());
        }

        // sign the APK with the supplied key/cert
        try {
            ZipSigner zipSigner = new ZipSigner();
            X509Certificate cert = zipSigner.readPublicKey(publicKeyUrl);
            PrivateKey privateKey = zipSigner.readPrivateKey(privateKeyUrl,  keyPassword);
            zipSigner.setKeys("xx", cert, privateKey, null);
            zipSigner.signZip(generatedApkPath, signedApkPath);
        } catch (Exception e) {
            Log.e("Signing apk", "Error: "+e.getMessage());
            callbackContext.error("ZipSigner Error: "+e.getMessage());
        }

        // After signing apk , delete unsigned apk
        try {
            new File(generatedApkPath).delete();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("ApkBuilder Error: "+e.getMessage());
        }
        callbackContext.success(signedApkPath);
    }

    /* unpack the zip into a temp dir, overwrite stuff with the things in sourcedir 
    */
    private void mergeDirectory(String defaultZip, String sourceFilesDir, String workdir) {
        extractToFolder(defaultZip, workdir);
        
    }

    private void extractToFolder(String zipfile, String tempdir) {
    	InputStream inputStream=null;
    	try {
        	FileInputStream zipStream = new FileInputStream(zipfile);
        	inputStream = new BufferedInputStream(zipStream);
            ZipInputStream zis = new ZipInputStream(inputStream);
            inputStream = zis;

            ZipEntry ze;
            byte[] buffer = new byte[32 * 1024];

            while ((ze = zis.getNextEntry()) != null)
            {
                String compressedName = ze.getName();

                if (ze.isDirectory()) {
                   File dir = new File(tempdir + compressedName);
                   dir.mkdirs();
                } else {
                    File file = new File(tempdir + compressedName);
                    file.getParentFile().mkdirs();
                    if(file.exists() || file.createNewFile()){
                        FileOutputStream fout = new FileOutputStream(file);
                        int count;
                        while ((count = zis.read(buffer)) != -1)
                        {
                            fout.write(buffer, 0, count);
                        }
                        fout.close();
                    }
                }
                zis.closeEntry();
            }
       } catch (Exception e) {
            Log.e(LOG_TAG, "Unzip error ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
