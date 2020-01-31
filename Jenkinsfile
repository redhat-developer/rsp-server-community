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
		stage ('Java 8 runtime') {    
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
