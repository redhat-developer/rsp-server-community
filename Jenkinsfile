#!/usr/bin/env groovy

pipeline {
	agent { label 'rhel7-micro' }
	
	options {
	   timeout(time: 1, unit: 'HOURS')
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
				stash includes: '**', name: 'source'
			}
		}
		stage('Install requirements') {
			steps {
				def nodeHome = tool 'nodejs-8.11.1'
				env.PATH="${env.PATH}:${nodeHome}/bin"
				sh "npm install -g typescript vsce"
			}
		}
		stage ('Build community server with Java 8 runtime') {    
			agent { label 'rhel7' }
			stages {
				stage('Build Java 8 & unit tests') {
					steps {
						unstash 'source'
						dir("rsp") {
							sh 'mvn clean install -fae -B'
						}
						archiveArtifacts 'rsp/distribution/distribution*/target/org.jboss.tools.rsp.distribution*.zip,rsp/site/target/repository/**'
						stash includes: 'rsp/distribution/distribution*/target/org.jboss.tools.rsp.distribution*.zip', name: 'zips'
						stash includes: 'rsp/site/target/repository/**', name: 'site'
					}
				}
				stage('SonarCloud Report') {
					when {
						expression { params.SONAR }
					}
					steps {
						sh 'mvn -B -P sonar sonar:sonar -Dsonar.login=${SONAR_TOKEN}'
					}
				}
			}
		}
		stage("Build extension") {
			dir("vscode") {
				sh "npm install"
				sh "npm run build"
			}
		}

		withEnv(['JUNIT_REPORT_PATH=report.xml', 'CODE_TESTS_WORKSPACE=c:/unknown']) {
			stage('Test') {
				wrap([$class: 'Xvnc']) {
					dir("vscode") {
						sh "npm test --silent"
						junit 'report.xml'
					}
				}
	                }
	        }

		stage('Package') {
			try {
				def packageJson = readJSON file: 'package.json'
				sh "vsce package -o adapters-${packageJson.version}-${env.BUILD_NUMBER}.vsix"
			}
			finally {
				archiveArtifacts artifacts: '*.vsix'
			}
		}
	}
	post {
		success {
			script {
				unstash 'site'
				unstash 'zips'
			}
		}
	}
}
