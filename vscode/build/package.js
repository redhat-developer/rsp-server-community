const fs = require('fs-extra');
const download = require('download');
const decompress = require('decompress');

const RSP_SERVER_JAR_NAME = 'org.example.rsp.server.wonka.distribution-0.20.0-SNAPSHOT.zip';
const RSP_SERVER_ZIP = __dirname + `/../../rsp/distribution/distribution.wonka/target/${RSP_SERVER_JAR_NAME}`;

function clean() {
    console.log(RSP_SERVER_ZIP);
    return Promise.resolve()
        .then(()=>fs.remove('server'))
        .then(()=>fs.pathExists(RSP_SERVER_JAR_NAME))
        .then((exists)=>(exists?fs.unlink(RSP_SERVER_JAR_NAME):undefined));
}

Promise.resolve()
    .then(clean)
    .then(()=> decompress(RSP_SERVER_ZIP, './server', { strip: 1 }))
    .catch((err)=>{ throw err; });
