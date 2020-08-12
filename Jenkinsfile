#!/usr/bin/env groovy

def prepareRemoteFolders(def emptyDir, def upload_dir, def distroVersion, def packageJson) {
	// Ensure proper folders are created
	echo "Creating empty remote dirs for given version output"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/plugins/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/${packageJson.version}/"

	// Ensure proper folders are created *AND* emptied
	echo "Ensuring some remote dirs are empty"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 --delete ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/plugins/"
	sh "rsync -Pzrlt --rsh=ssh --protocol=28 --delete ${emptyDir}/ ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/${packageJson.version}/"
}

pipeline {
	agent { label 'rhel8' }
	
	options {
		timeout(time: 30, unit: 'MINUTES')
		timestamps()
	}

	tools {
		maven 'maven3-latest'
		jdk 'openjdk-1.8'
	}
	
	stages {
		stage('Checkout SCM') {
			steps {
				deleteDir()
				git url: "https://github.com/${params.FORK}/rsp-server-community", branch: params.BRANCH
			}
		}
		stage('Install requirements') {
			steps {
				script {
					def nodeHome = tool 'nodejs-12.13.1'
					env.PATH="${env.PATH}:${nodeHome}/bin"
				}
				sh "npm install -g typescript vsce"
			}
		}
		stage ('Build community server with Java 8 runtime') {
			steps {
				dir("rsp") {
					sh 'mvn clean install -fae -B'
				}
				archiveArtifacts 'rsp/distribution/distribution*/target/org.jboss.tools.rsp.*.zip,rsp/site/target/repository/**'
			}
		}
		stage('SonarCloud Report') {
			when {
				expression { params.SONAR }
			}
			steps {
				dir( "rsp" ) {
					script {
						withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
							sh '''
								set +x
								mvn -B -P sonar sonar:sonar -Dsonar.login="${SONAR_TOKEN}"
							'''
						}
					}
				}
			}
		}
		stage("Build extension") {
			steps {
				dir("vscode") {
					sh 'rm package-lock.json'
					sh "npm install"
					sh "npm run build"
				}
			}
		}
		
		stage('Test extension') {
			steps {
				withEnv(['JUNIT_REPORT_PATH=report.xml', 'CODE_TESTS_WORKSPACE=c:/unknown']) {
					wrap([$class: 'Xvnc', takeScreenshot: false, useXauthority: true]) {
						dir("vscode") {
							sh "npm test --silent"
							junit 'report.xml'
						}
					}
				}
			}
		}

		stage('Package VSCode extension') {
			steps {
				script {
					dir("vscode") {
						try {
							def packageJson = readJSON file: 'package.json'
							sh "vsce package -o adapters-community-${packageJson.version}-${env.BUILD_NUMBER}.vsix"
						}
						finally {
							archiveArtifacts artifacts: '*.vsix'
						}
					}
				}
			}
		}
	}
	post {
		success {
			script {
				stage('Uploading bits') {
					sh "ls *"
					def packageJson = readJSON file: 'vscode/package.json'

					// If releasing push files into stable directory
					def upload_dir = params.publishToMarketPlace ? 'stable' : 'snapshots'
					sh "echo 'Pushing bits into ${upload_dir}'"

					// Make an empty directory
					sh "echo Creating empty directory"
					def emptyDir = sh script: "mktemp -d | tr -d '\n'", returnStdout: true
					sh "chmod 775 ${emptyDir}"

					def distroVersion = sh script: "ls rsp/distribution/distribution.community/target/*.zip | cut --complement -f 1 -d '-' | rev | cut -c5- | rev | tr -d '\n'", returnStdout: true
					sh "echo Distro version: ${distroVersion}"

					// prepare remote folder structure
					sh "echo Preparing remote folders"
					prepareRemoteFolders(emptyDir, upload_dir, distroVersion, packageJson)

					// Begin Copying Files Over and publish to marketplace

					// Upload the p2 update site.  This logic only works because all plugins are jars. 
					// If we ever have exploded bundles here, this will need to be redone
					sh "echo Uploading site"
					def siteRepositoryFilesToPush = findFiles(glob: 'rsp/site/target/repository/*')
					for (i = 0; i < siteRepositoryFilesToPush.length; i++) {
						sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${siteRepositoryFilesToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/"
					}

					sh "echo Uploading site/plugins"
					def sitePluginFilesToPush = findFiles(glob: 'rsp/site/target/repository/plugins/*')
					for (i = 0; i < sitePluginFilesToPush.length; i++) {
						sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${sitePluginFilesToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/plugins/"
					}

					// Upload distributions / zips
					sh "echo Uploading distro files"
					def filesToPush = findFiles(glob: '**/*.zip')
					for (i = 0; i < filesToPush.length; i++) {
						sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${filesToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/"
					}

					// Upload VSIX file
					sh "echo Uploading vsix"
					def vsixToPush = findFiles(glob: '**/*.vsix')
					for (i = 0; i < vsixToPush.length; i++) {
						sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${vsixToPush[i].path} ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/${packageJson.version}/"
					}

					// publish to market place
					if (params.publishToMarketPlace) {
						stage('Publish to Marketplace') {
							timeout(time:5, unit:'DAYS') {
								input message:'Approve publishing to MarketPlace?'
							}
							sh "echo 'Publishing to marketplace'"
							withCredentials([[$class: 'StringBinding', credentialsId: 'vscode_java_marketplace', variable: 'TOKEN']]) {
								def vsix = findFiles(glob: '**/*.vsix')
								sh "echo Publishing ${vsix[0].path}"
								sh 'vsce publish -p ${TOKEN} --packagePath' + " ${vsix[0].path}"
							}
						}
					}
				}
			}
		}
	}
}
