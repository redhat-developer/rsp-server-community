const fs = require('fs-extra');
const download = require('download');
const decompress = require('decompress');

const RSP_SERVER_JAR_PREFIX = 'org.example.rsp.server.extras.distribution-';
const RSP_DIR_TO_SEARCH = __dirname + `/../../rsp/distribution/distribution.community/target/`

const RSP_FOUND_DISTRO_NAME = findZip(RSP_DIR_TO_SEARCH);
const RSP_FOUND_DISTRO_FULL_PATH = RSP_DIR_TO_SEARCH + RSP_FOUND_DISTRO_NAME;


function clean() {
	return Promise.resolve()
		.then(()=>fs.remove('server'))
		.then(()=>fs.pathExists(RSP_FOUND_DISTRO_NAME))
		.then((exists)=>(exists?fs.unlink(RSP_FOUND_DISTRO_NAME):undefined));
}

function findZip(basedir) {
	var result = "";
	fs.readdirSync(basedir).forEach(file => {
		if( file.endsWith("zip")) {
			result = file;
		}
	});
	return result;
}


Promise.resolve()
    .then(clean)
    .then(()=> decompress(RSP_FOUND_DISTRO_FULL_PATH, './server', { strip: 1 }))
    .catch((err)=>{ throw err; });
