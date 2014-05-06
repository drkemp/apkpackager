

function pkgSuccess( apkpath ) {

}

function pkgFail(msg) {
}

function onInitFs(fs) {

  // need a native compatible absolute path that ends with /
  var workdir = 'where is the dummy stuff and APK';
  var wwwdir = 'where is your www';
  var publicKeyURL = "file://"+workdir+"pub.x509.pem";
  var privateKeyUrl = "file://"+workdir+"pk8p.pk8");
  var passwd="android";      // password for private key
  exec(pkgSuccess, pkgFail, APKPackager, 'package', [wwwdir, workdir, publicKeyURL, privateKeyURL, passwd]);
}

function errorHander(msg) {

  console.log('Error: ' + msg);
}

window.requestFileSystem(window.PERMANENT, 20*1024*1024, onInitFs, errorHandler);


