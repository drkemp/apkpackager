// Copyright (c) 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.apkpackager;

import java.io.File;
import java.net.URL;
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
import org.json.JSONObject;


public class APKPackager  extends CordovaPlugin {

    private static CordovaWebView webView;
  
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        APKPackager.webView = webView;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("package".equals(action)) {
            package(args, callbackContext);
            return true;
        }
        return false;
    }
    private void package(CordovaArgs args, CallbackContext callbackContext) {
        String publicKeyURL= args.getString(0);
        String privateKeyURL= args.getString(1);
        String password= args.getString(2);
        String workdir= args.getString(3);
        doit(publicKeyURL, privateKeyURL, password, workdir);
    }
    public void doit(URL publicKeyUrl, URL privateKeyUrl, String keyPassword, String workdir) {
    
        String apkname = workdir+"test.apk";
        String signapkame=workdir+"test-signed.apk";
        String resname =workdir+"res.zip";
        String assetsname =workdir+"assets.zip";
        String dexname = workdir+"classes.dex";
        String wwwname=workdir+"www.zip";
        String maniname = workdir+"AndroidManifest.xml";
		
        createAPK(apkname, maniname, resname, assetsname, dexname,wwwname);
        signAPK(publicKeyUrl, privateKeyUrl, keyPassword, apkname,signapkame);
    }

    public String createAPK(String apkpath, String maniname, String respath, String assetsname, String dexpath, String wwwpath) {
        try{
            ApkBuilder b = new ApkBuilder(apkpath,respath,dexpath,null,null,null);
            b.addZipFile(new File(wwwpath));
            b.addZipFile(new File(assetsname));
            b.addFile(new File(maniname),"AndroidManifest.xml");
            b.sealApk();
        } catch (Exception e) {
            Log.e("ApkBuilder",e.getMessage());
        }
        return apkpath;
    }
    public void signAPK(URL publicKeyUrl, URL privateKeyUrl,String keyPassword, String generatedApkPath, String signedApkPath) {
        try {
            // use SHA1 with RSA 2048, 10000 days,
            Log.i("Sign APK", "Creating signed apk as:" + signedApkPath);
            ZipSigner zipSigner = new ZipSigner();
            X509Certificate bob = zipSigner.readPublicKey(publicKeyUrl);
            PrivateKey privateKey = zipSigner.readPrivateKey(privateKeyUrl,  keyPassword);
            zipSigner.setKeys("xx", bob, privateKey,null);
            zipSigner.signZip(generatedApkPath, signedApkPath);
            // After signing apk , delete unsigned apk
            new File(generatedApkPath).delete();
            Log.i("Sign APK", "signed.apk success");
        } catch (Throwable t) {
            Log.e("Signing apk", "Error while signing apk", t);
            t.printStackTrace();
        }
    }
}
