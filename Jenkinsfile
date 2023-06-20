#!/usr/bin/env groovy

def prepareRemoteFolders(def emptyDir, def upload_dir, def distroVersion, def packageJson) {
	// Ensure proper folders are created
	echo "Creating empty remote dirs for given version output"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/plugins/'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir} <<< \$'mkdir ${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/${packageJson.version}/'"

	// Ensure proper folders are created *AND* emptied
	echo "Ensuring some remote dirs are empty"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/plugins/ <<< \$'rm *'"
	sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/${packageJson.version}/ <<< \$'rm *'"
}

pipeline {
	agent { label 'rhel8' }
	
	options {
		timeout(time: 30, unit: 'MINUTES')
		timestamps()
	}

	tools {
		maven 'maven3-latest'
		jdk 'openjdk-11'
	}
	
	stages {
		stage('Checkout SCM') {
			steps {
				deleteDir()
				git url: "https://github.com/${params.FORK}/rsp-server-community.git", branch: params.BRANCH
			}
		}
		stage('Install requirements') {
			steps {
				script {
					def nodeHome = tool 'nodejs-lts'
					env.PATH="${env.PATH}:${nodeHome}/bin"
				}
				sh "npm install -g typescript || true"
				sh "npm install -g vsce || true"
				sh "npm install -g ovsx || true"
			}
		}
		stage ('Build community server with Java 11 runtime') {
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
						sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/ <<< \$'put -p ${siteRepositoryFilesToPush[i].path}'"
					}

					sh "echo Uploading site/plugins"
					def sitePluginFilesToPush = findFiles(glob: 'rsp/site/target/repository/plugins/*')
					for (i = 0; i < sitePluginFilesToPush.length; i++) {
						sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/p2/plugins/ <<< \$'put -p ${sitePluginFilesToPush[i].path}'"
					}

					// Upload distributions / zips
					sh "echo Uploading distro files"
					def filesToPush = findFiles(glob: '**/*.zip')
					for (i = 0; i < filesToPush.length; i++) {
						sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/${distroVersion}/ <<< \$'put -p ${filesToPush[i].path}'"
					}

					// Upload VSIX file
					sh "echo Uploading vsix"
					def vsixToPush = findFiles(glob: '**/*.vsix')
					for (i = 0; i < vsixToPush.length; i++) {
						sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/vscode-extension/${packageJson.version}/ <<< \$'put -p ${vsixToPush[i].path}'"
					}

					sh "echo org.jboss.tools.rsp.community.distribution.latest.version=${distroVersion} > LATEST"
					sh "echo org.jboss.tools.rsp.community.distribution.latest.url=https://download.jboss.org/jbosstools/adapters/${upload_dir}/rsp-server-community/distributions/${distroVersion}/org.jboss.tools.rsp.server.community.distribution-${distroVersion}.zip >> LATEST"
					sh "sftp -C ${UPLOAD_USER_AT_HOST}:${UPLOAD_PATH}/${upload_dir}/rsp-server-community/distributions/ <<< \$'put -p LATEST'"


                                        // publish to ovsx
                                        if (params.publishToOVSX) {
                                                stage('Publish to OVSX') {
                                                        timeout(time:5, unit:'DAYS') {
                                                                input message:'Approve publishing to OVSX?'
                                                        }
                                                        sh "echo 'Publishing to OVSX'"
							withCredentials([[$class: 'StringBinding', credentialsId: 'open-vsx-access-token', variable: 'OVSX_TOKEN']]) {
                                                                def vsix = findFiles(glob: '**/*.vsix')
                                                                sh "echo Publishing ${vsix[0].path}"
                                                                sh 'ovsx publish -p ${OVSX_TOKEN} --packagePath' + " ${vsix[0].path}"
                                                        }
                                                }
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
