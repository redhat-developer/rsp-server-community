#!/bin/sh
apiStatus=`git status -s | wc -l`
if [ $apiStatus -ne 0 ]; then
   echo "This repository has changes and we won't be able to auto upversion. Please commit or stash your changes and try again"
   exit 1
fi

cd rsp
oldver=`cat pom.xml  | grep "version" | head -n 2 | tail -n 1 | cut -f 2 -d ">" | cut -f 1 -d "<" | sed 's/\.Final//g' | awk '{$1=$1};1'`

newLastSegment=`echo $oldver | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
newverPrefix=`echo $oldver | cut -f 1,2 -d "."`
newver=$newverPrefix.$newLastSegment

echo "Old version is $oldver"
echo "New version is $newver"
echo "Updating pom.xml with new version"
mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$newver.Final

# Handle target platform
tpFile=`ls -1 targetplatform | grep target`
cat targetplatform/$tpFile | sed "s/-target-$oldver/-target-$newver/g" > targetplatform/$tpFile.bak
mv targetplatform/$tpFile.bak targetplatform/$tpFile
echo "Please go update the TP to depend on newest rsp-server if required"
read -p "Press enter to continue"



mvn clean install -DskipTests
echo "Did it succeed?"
read -p "Press enter to continue"

cd ../vscode/

oldvervsc=`cat package.json  | grep "\"version\":" | cut -f 2 -d ":" | sed 's/"//g' | sed 's/,//g' | awk '{$1=$1};1'`

newLastSegmentvsc=`echo $oldvervsc | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
newverPrefixvsc=`echo $oldvervsc | cut -f 1,2 -d "."`
newvervsc=$newverPrefixvsc.$newLastSegmentvsc

echo "Old version is $oldvervsc"
echo "New version is $newvervsc"
echo "Updating package.json with new version"
cat package.json | sed "s/  \"version\": \"$oldver\",/  \"version\": \"$newver\",/g" > package2
mv package2 package.json
echo "Running npm install"
npm install

npm run build
echo "Did it succeed?"
read -p "Press enter to continue"

echo "Running vsce package"
vsce package

echo "Committing and pushing to main"
git commit -a -m "Upversion to $newver for release" --signoff
curBranch=`git rev-parse --abbrev-ref HEAD`
git push origin $curBranch


echo "Go kick a Jenkins Job please. Let me know when it's DONE and green."
read -p "Press enter to continue"

echo "Go kick another jenkins job with a release flag."
read -p "Press enter to continue"


echo "Are you absolutely sure you want to tag?"
read -p "Press enter to continue"

newVerUnderscore=`echo $newvervsc | sed 's/\./_/g'`
git tag v$newVerUnderscore
git push origin v$newVerUnderscore


echo "Make sure to go create a release on github"
echo "Here are the commits since last release"

commits=`git lg | grep -n -m 2 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`
git lg | head -n $commits



