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
				git url: "https://github.com/${params.FORK}/rsp-server.git", branch: params.BRANCH
				stash includes: '**', name: 'source'
			}
        }
        stage ('Running parallel branches') {
    	    parallel {
    	        stage ('Java 8 runtime') {
    	            
    	            agent { label 'rhel7' }
    	            
    	            stages {
    
                		stage('Build Java 8 & unit tests') {
                			steps {
                				unstash 'source'
                				sh 'mvn clean install -fae -B'
                				archiveArtifacts 'distribution/distribution*/target/org.jboss.tools.rsp.distribution*.zip,api/docs/org.jboss.tools.rsp.schema/target/*.jar'
                				stash includes: 'distribution/distribution*/target/org.jboss.tools.rsp.distribution*.zip,api/docs/org.jboss.tools.rsp.schema/target/*.jar', name: 'zips'
                        	}
                		}
                		
                        stage('Integration tests') {
                			steps {
                				sh 'mvn verify -B -Pintegration-tests -DskipTests=true -Dmaven.test.failure.ignore=true'
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
                	post {
                	    always {
                	        junit '**/surefire-reports/*.xml'
                	        archiveArtifacts '**/integration-tests/target/surefire-reports/*,**/tests/**/target/surefire-reports/*'
                	    }
                	}
    	        }
    	        
    	        stage ('Java 11 runtime') {
    	            
    	            agent { label 'rhel7' }
    	            
    	            tools {
                    	jdk 'openjdk11'
                    }
                    
    	            stages {
    
                		stage('Build Java 11 & unit tests') {
                			steps {
                				unstash 'source'
                				sh 'mvn clean install -fae -B'
                        	}
                		}
                		
                        stage('Integration tests') {
                			steps {
                				sh 'mvn verify -B -Pintegration-tests -DskipTests=true -Dmaven.test.failure.ignore=true'
                        	}
                        }
    	            }
                	post {
                	    always {
                	        junit '**/surefire-reports/*.xml'
                	        archiveArtifacts '**/integration-tests/target/surefire-reports/*,**/tests/**/target/surefire-reports/*'
                	    }
                	}
                }
    	    }
    	}
	}
	post {
	    success {
			script {
			    unstash 'zips'
				def filesToPush = findFiles(glob: '**/*.zip')
				for (i = 0; i < filesToPush.length; i++) {
					sh "rsync -Pzrlt --rsh=ssh --protocol=28 ${filesToPush[i].path} ${UPLOAD_LOCATION}/snapshots/rsp-server/"
				}
			}
	    }
	}
}
