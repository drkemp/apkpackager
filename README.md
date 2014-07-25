# Android Package to build an apk on device

This plugin provides the packaging (zip, zipalign) and signing methods

## Status

Unstable/in development on Android. 
Not suitable for other platforms

## Notes

This demo functionality requires that the following are available in the Downloads directory of the device
* a directory containing the custom ww assets (wwwsrc)
* a directory containing the custom res (ressrc)
* a file containing the dexed code (classes.dex)
* a zip file of the default res (res.zip)
* a zip file of the default assests (assets.zip)
* the www directory to include
* the developers public key (pub.x509.pem)
* the developers private key (pk8p.pk8)
* the keystore passphrase must be 'android'

