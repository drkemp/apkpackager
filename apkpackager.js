
var exec = cordova.require('cordova/exec');


module.exports.makeapk = function(pkguri,success,failure) {

  function pkgSuccess( apkpath ) {
     if(success) success();
  }

  function pkgFail(msg) {
    console.log('Error: ' + msg);
    if(failure) failure(msg);
  }

//  function onInitFs(fs) {

    // need a native compatible absolute path that ends with /
    var workdir = 'cdvfile://localhost/sdcard/Download/'; //fs.root.toURL()+'Download/';
    var wwwdir = workdir+'wwwsrc';
    var resdir = workdir+'ressrc';
    var publicKeyURL = workdir+"pub.x509.pem";
    var privateKeyURL = workdir+"pk8p.pk8";
    var passwd="android";      // password for private key
    exec(pkgSuccess, pkgFail, 'APKPackager', 'package', [wwwdir, resdir, workdir, publicKeyURL, privateKeyURL, passwd]);
//  }

//  window.requestFileSystem(window.PERSISTENT, 20*1024*1024, onInitFs, pkgFail);

}

