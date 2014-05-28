// Copyright (c) 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.BufferedInputStream;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import kellinwood.security.zipsigner.ZipSigner;
import android.net.Uri;
import android.util.Log;
import com.android.sdklib.build.*;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.chromium.aapt.Driver;
import org.json.JSONException;


public class APKPackager  extends CordovaPlugin {

    private String LOG_TAG = "APKPackage";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
  
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("package".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    packageApk(args, callbackContext);
                }
            });
            return true;
        }
        return false;
    }

    private void packageApk(CordovaArgs args, CallbackContext callbackContext) {
    	File wwwdir=null;
    	File resdir=null;
    	File workdir=null;
    	URL publicKeyUrl=null;
    	URL privateKeyUrl=null;
    	String keyPassword="";
    
    	try {
    		CordovaResourceApi cra = webView.getResourceApi();
    		wwwdir = cra.mapUriToFile(cra.remapUri(Uri.parse(args.getString(0))));
	        resdir = cra.mapUriToFile(cra.remapUri(Uri.parse(args.getString(1))));
	        workdir= cra.mapUriToFile(cra.remapUri(Uri.parse(args.getString(2))));
	        File pbk = cra.mapUriToFile(cra.remapUri(Uri.parse(args.getString(3))));
	        File pvk = cra.mapUriToFile(cra.remapUri(Uri.parse(args.getString(4))));
	        publicKeyUrl = pbk.toURI().toURL();
	        privateKeyUrl= pvk.toURI().toURL();
	        keyPassword= args.getString(5);
        } catch (Exception e) {
            callbackContext.error("Missing arguments: "+e.getMessage());
            return;
        }
        File reszip = new File (workdir, "res.zip");
        File assetsname = new File(workdir, "assets.zip");

    	String workdirpath=workdir.getAbsolutePath()+File.separator;
        String generatedApkPath = workdirpath+"test.apk";
        String signedApkPath=workdirpath+"test-signed.apk";
        String dexname = workdirpath+ "classes.dex";

        File tempres = new File(workdir,"tempres");
        File tempassets = new File(workdir,"tempasset");
        File mangledResourceDir= new File(workdir, "binres");
        File finalResDir =new File(mangledResourceDir,"res");

        try {
        	deleteDir(tempres);
        	deleteDir(tempassets);
        	deleteDir(mangledResourceDir);
        	deleteDir(finalResDir);
        	mangledResourceDir.mkdirs();
        } catch (Exception e) {
            callbackContext.error("Unable to delete dirs: "+e.getMessage());
            return;
        }
        try {
            extractToFolder(assetsname, tempassets);
            extractToFolder(reszip, tempres);
        } catch (Exception e) {
            callbackContext.error("Unable to extract project: "+e.getMessage());
            return;
        }
        
        try {
            // merge the supplied www & res dirs into the dummy project
            // for this to work the relative path of the supplied dir must be the same as the desired path in the APK
            // ie. ./foo/bar.png with be at /foo/bar.png in the APK
            mergeDirectory(wwwdir, tempassets);
            mergeDirectory(resdir, tempres);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("Error merging assets: "+e.getMessage());
            return;
        }

        try {
            mungeConfig(workdir, tempassets, tempres);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("Error updating project: "+e.getMessage());
            return;
        }

        try {
            copyFile(new File(workdir,"AndroidManifest.xml"), new File(tempres,"AndroidManifest.xml"));
            mangleResources(tempres, mangledResourceDir);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("Error indexing resources: "+e.getMessage());
            return;
        }

        // take the completed package and make the unsigned APK
        try{
            // ApkBuilder REALLY wants a resource zip file in the contructor
            // but the composite res is not a zip - so hand it a dummy
            File fakeResZip = new File(workdir,"FakeResourceZipFile.zip");
            writeZipfile(fakeResZip);

            ApkBuilder b = new ApkBuilder(generatedApkPath,fakeResZip.getPath(),dexname,null,null,null);
            b.addSourceFolder( tempassets);
            b.addSourceFolder( finalResDir);
            b.addFile(new File(mangledResourceDir,"resources.arsc"), "resources.arsc");
            b.addFile(new File(mangledResourceDir,"AndroidManifest.xml"),"AndroidManifest.xml");
            b.sealApk();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("ApkBuilder Error: "+e.getMessage());
            return;
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
            return;
        }

        // After signing apk , delete intermediate stuff
        try {
            new File(generatedApkPath).delete();
            deleteDir(tempres);
            deleteDir(tempassets);
            deleteDir(mangledResourceDir);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error("Error cleaning up: "+e.getMessage());
            return;
        }
        callbackContext.success(signedApkPath);
    }

    private void deleteDir(File dir){
        if(!dir.exists()) return;
        if(dir.isDirectory()) {
            File [] files = dir.listFiles();
            if(files != null) {
                for( File f : files ) {
    	            if(f.isDirectory()) deleteDir(f);
    	            else f.delete();
                }
            }
        }
        dir.delete();
    }
    private void writeZipfile(File zipFile) throws IOException {
        if(zipFile.exists()) zipFile.delete();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry e = new ZipEntry("dummydir");
        out.putNextEntry(e);
        out.closeEntry();
        out.close();
    }
    private void mangleResources(File srcResDir, File targetdir) {
    	//TODO : put useful stuff here
    	Driver d = new Driver(srcResDir, targetdir);
    	try {
    		File outputFile = new File(targetdir, "resources.arsc");
    		OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
			d.createResourceTable(os);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void writeStringToFile(String str, File target) {
    	FileWriter fw=null;
    	try {
    		File dir = target.getParentFile();
    		if(!dir.exists()) dir.mkdirs();
			fw = new FileWriter(target);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
			fw.close();
			} catch(Exception e) {}
		}
    	
    }
    
    private void mungeConfig(File workdir, File tempassets, File tempres) {
    	// rewrite the app package, app name, permissions
    	// get stuff from tempassets/manifest.mobile.json
    	// update strings in tempres
    	// update AndroidManifest.xml in workdir
    }

    /* overwrite stuff from a default zip with the things in sourcedir 
    */
    private void mergeDirectory(File srcdir, File workdir) 
            throws FileNotFoundException, IOException {
        File[] files = srcdir.listFiles();
        for(File file : files){
            if(file.isDirectory()) {
                File targetDir = new File(workdir, file.getName());
                targetDir.mkdirs();
                mergeDirectory(file, targetDir);
            } else {
                File targetFile = new File(workdir, file.getName());
                if(targetFile.exists()) targetFile.delete();
                copyFile(file, targetFile);
            }
        }
    }

    private void copyFile(File src, File dest)
            throws FileNotFoundException, IOException {
        FileInputStream istream = new FileInputStream(src);
        FileOutputStream ostream = new FileOutputStream(dest);
        FileChannel input = istream.getChannel();
        FileChannel output = ostream.getChannel();

        try {
            input.transferTo(0, input.size(), output);
        } finally {
            istream.close();
            ostream.close();
            input.close();
            output.close();
        }
    }

    private void extractToFolder(File zipfile, File tempdir) {
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
                   File dir = new File(tempdir, compressedName);
                   dir.mkdirs();
                } else {
                    File file = new File(tempdir, compressedName);
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
