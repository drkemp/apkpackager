package org.chromium.apkpackager;

import java.io.File;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import kellinwood.security.zipsigner.ZipSigner;
import android.util.Log;
import com.android.sdklib.build.*;

public class APKPackager {
  
  public void blah(){
    File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    String workdir=f.getAbsolutePath();
    if(!workdir.endsWith("/")) {
      workdir=workdir.concat("/");
    }
    try{
      // yyyy.x509.pem
      URL publicKeyUrl= new URL("file://"+workdir+"pub.x509.pem");
      // xxxx.pk8
      String privfile="pk8p.pk8";
      URL privateKeyUrl = new URL("file://"+workdir+privfile);
      String passwd="android";      // password for private key
      com.bobo.fred.Builder.doit(publicKeyUrl,privateKeyUrl,passwd,workdir);
    } catch (Exception e) {
      Log.e("Apk Launch",e.getMessage());
    }

  }
	public static void doit(URL publicKeyUrl, URL privateKeyUrl, String keyPassword, String workdir) {
		org.chromium.apkpackager.APKPackager b = new org.chromium.apkpackager.APKPackager();
    
		String apkname = workdir+"test.apk";
		String signapkame=workdir+"test-signed.apk";
		String resname =workdir+"res.zip";
		String assname =workdir+"assets.zip";
		String dexname = workdir+"classes.dex";
		String wwwname=workdir+"www.zip";
		String maniname = workdir+"AndroidManifest.xml";
		
		b.createAPK(apkname, maniname, resname, assname, dexname,wwwname);
		b.signAPK(publicKeyUrl, privateKeyUrl, keyPassword, apkname,signapkame);
	}
	
	public String createAPK(String apkpath, String maniname, String respath, String assname, String dexpath, String wwwpath) {
		try{
			ApkBuilder b = new ApkBuilder(apkpath,respath,dexpath,null,null,null);
			b.addZipFile(new File(wwwpath));
			b.addZipFile(new File(assname));
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
