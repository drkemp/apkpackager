

function pkgSuccess( path ) {
}

function pkgFail(msg) {
}

function onInitFs(fs) {

  // need a native compatible absolute path that ends with /
  var workdir = fs.root.localURL();
  var publicKeyURL = "file://"+workdir+"pub.x509.pem";
  var privateKeyUrl = "file://"+workdir+"pk8p.pk8");
  var passwd="android";      // password for private key
  exec(pkgSuccess, pkgFail, APKPackager, 'package', [publicKeyURL, privateKeyURL, passwd, workdir]);
}

function errorHander(msg) {

  console.log('Error: ' + msg);
}

window.requestFileSystem(window.PERMANENT, 20*1024*1024, onInitFs, errorHandler);


