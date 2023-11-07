#!/bin/sh
repoOwnerAndName=redhat-developer/rsp-server-community
curBranch=`git rev-parse --abbrev-ref HEAD`
ghtoken=`cat ~/.keys/gh_access_token`
argsPassed=$#
echo "args: " $argsPassed
if [ "$argsPassed" -eq 1 ]; then
	debug=1
	echo "YOU ARE IN DEBUG MODE. Changes will NOT be pushed upstream"
else
	echo "The script is live. All changes will be pushed, deployed, etc. Live."
	debug=0
fi
read -p "Press enter to continue"

echo ""
echo "Please go run rm -rf ~/.m2/repository/*"
read -p "Press enter to continue"

apiStatus=`git status -s | wc -l`
if [ $apiStatus -ne 0 ]; then
   echo "This repository has changes and we won't be able to auto upversion. Please commit or stash your changes and try again"
   exit 1
fi

echo ""
echo "These are the commits for the release"
commits=`git lg | grep -n -m 1 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`
commitMsgs=`git log --color --pretty=format:'%h - %s' --abbrev-commit | head -n $commits`
echo "$commitMsgs"
read -p "Press enter to continue"


cd rsp
oldverRspRaw=`cat pom.xml  | grep "version" | head -n 2 | tail -n 1 | cut -f 2 -d ">" | cut -f 1 -d "<" |  awk '{$1=$1};1'`
oldverRsp=`echo $oldverRspRaw | sed 's/\.Final//g' | sed 's/-SNAPSHOT//g'`
oldverRspHasSnapshot=`cat pom.xml  | grep "version" | head -n 2 | tail -n 1 | cut -f 2 -d ">" | cut -f 1 -d "<" | grep -i snapshot | awk '{$1=$1};1' | wc -c`


if [ "$oldverRspHasSnapshot" -eq 0 ]; then
	newLastSegmentRsp=`echo $oldverRspRaw | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
	newverPrefixRsp=`echo $oldverRspRaw | cut -f 1,2 -d "."`
	newverRsp=$newverPrefixRsp.$newLastSegmentRsp
else 
	newverRsp=$oldverRsp
fi
newverRspFinal=$newverRsp.Final

echo "Old version (RSP) is $oldverRspRaw"
echo "New version (RSP) is $newverRspFinal"


# Handle target platform
echo "Updating target platform with new version"
tpFile=`ls -1 targetplatform | grep target`
cat targetplatform/$tpFile | sed "s/-target-$oldver/-target-$newver/g" > targetplatform/$tpFile.bak
mv targetplatform/$tpFile.bak targetplatform/$tpFile

latestRspServerVersion=`curl https://download.jboss.org/jbosstools/adapters/stable/rsp-server/LATEST | grep version | cut -f 2 -d "="`
echo ""
echo "We will now depend on the latest rsp-server $latestRspServerVersion"
read -p "Press enter to continue"

cat targetplatform/rsp-community-target.target | sed "s/rsp-server\/p2\/.*/rsp-server\/p2\/$latestRspServerVersion\/\"\/>/g" > targetplatform/rsp-community-target.target2

mv targetplatform/rsp-community-target.target2 targetplatform/rsp-community-target.target

echo "Done with target file..."
read -p "Press enter to continue"



echo "Updating pom.xml with new version"
mvn org.eclipse.tycho:tycho-versions-plugin:1.3.0:set-version -DnewVersion=$newverRspFinal

echo "Lets build the RSP"
read -p "Press enter to continue"
mvn clean install -DskipTests
echo "Did it succeed?"
read -p "Press enter to continue"

echo ""
echo "Looks like its time to build the extension now"
read -p "Press enter to continue"

cd ../vscode/

oldvervsc=`cat package.json  | grep "\"version\":" | cut -f 2 -d ":" | sed 's/"//g' | sed 's/,//g' | awk '{$1=$1};1'`

echo "Old version [vsc extension] is $oldvervsc"
echo "Running npm install"
npm install

npm run build
echo "Did it succeed?"
read -p "Press enter to continue"

echo ""
echo ""
msgLine1=`ls server/bundle/*spi* | cut -f 2 -d "_" | cut -f 1,2,3 -d "." | awk '{ print "Now using the " $0 " release of rsp-server. "}'`
echo $msgLine1

echo "Running vsce package"
vsce package
echo "Did it succeed?"
read -p "Press enter to continue"


echo "Go kick a Jenkins Job please. Come back when its DONE and green."
read -p "Press enter to continue"


oldVerVscUnderscore=`echo $oldvervsc | sed 's/\./_/g'`
oldVerVscFinal=$oldvervsc.Final
vscTagName=v$oldVerVscUnderscore.Final

echo "Committing and pushing to $curBranch"
git commit -a -m "Move extension to $vscTagName for release" --signoff

if [ "$debug" -eq 0 ]; then
	git push origin $curBranch
else 
	echo git push origin $curBranch
fi


echo "Go kick another jenkins job with a release flag."
read -p "Press enter to continue"



echo "Are you absolutely sure you want to tag?"
read -p "Press enter to continue"

git tag $vscTagName
if [ "$debug" -eq 0 ]; then
	git push origin $vscTagName
else 
	echo git push origin $vscTagName
fi


echo "Making a release on github for $oldVerVscFinal"
commitMsgsClean=`git log --color --pretty=format:'%s' --abbrev-commit | head -n $commits | awk '{ print " * " $0;}' | awk '{printf "%s\\\\n", $0}' | sed 's/"/\\"/g'`
msgLine1=`ls server/bundle/*spi* | cut -f 2 -d "_" | cut -f 1,2,3 -d "." | awk '{ print "Now using the " $0 " release of rsp-server. "}'`
msgLine2=`ls server/bundle/*spi* | cut -f 2 -d "_" | cut -f 1,2,3 -d "." | sed 's/\./_/g' | awk '{ print "See rsp-server CHANGELOG at https://github.com/redhat-developer/rsp-server/releases/tag/v" $0;}'`
commitMsgsFinal="$msgLine1\n$msgLine2\n$commitMsgsClean"

echo "Release commit log: $commitMsgsFinal"
read -p "Press enter to continue"


createReleasePayload="{\"tag_name\":\"$vscTagName\",\"target_commitish\":\"$curBranch\",\"name\":\"$oldVerVscFinal\",\"body\":\"Release of $oldVerVscFinal:\n\n"$commitMsgsFinal"\",\"draft\":false,\"prerelease\":false,\"generate_release_notes\":false}"

if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/$repoOwnerAndName/releases \
	  -d "$createReleasePayload" | tee createReleaseResponse.json
else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/$repoOwnerAndName/releases \
	  -d "$createReleasePayload"
fi

echo "Please go verify the release looks correct. We will add the asset next"
read -p "Press enter to continue"


assetUrl=`cat createReleaseResponse.json | grep assets_url | cut -c 1-17 --complement | rev | cut -c3- | rev | sed 's/api.github.com/uploads.github.com/g'`
rm createReleaseResponse.json
zipFileName=` ls -1 -t *.vsix  | head -n 1`
echo "Running command to add artifact to release: "
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$zipFileName \
	  --data-binary "@$zipFileName"
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$zipFileName \
	  --data-binary "@$zipFileName"
fi
echo ""
echo "Please go verify the release looks correct and the distribution was added correctly."
read -p "Press enter to continue"


echo ""
echo ""
echo "Time to update versions for development"
read -p "Press enter to continue"

cd ../rsp
echo "First the rsp"
read -p "Press enter to continue"
nextLastSegmentRsp=`echo $newverRsp | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
nextVerPrefixRsp=`echo $newverRsp | cut -f 1,2 -d "."`
nextVerRsp=$nextVerPrefixRsp.$nextLastSegmentRsp

echo "Current version (RSP) is $newverRsp"
echo "Next version (RSP) is $nextVerRsp"
echo "Updating pom.xml with new version"
mvn org.eclipse.tycho:tycho-versions-plugin:1.3.0:set-version -DnewVersion=$nextVerRsp-SNAPSHOT

# Handle target platform
tpFile=`ls -1 targetplatform | grep target`
cat targetplatform/$tpFile | sed "s/-target-$newverRsp.Final/-target-$nextVerRsp-SNAPSHOT/g" > targetplatform/$tpFile.bak
mv targetplatform/$tpFile.bak targetplatform/$tpFile

echo "Lets build the RSP After upversion"
read -p "Press enter to continue"
mvn clean install -DskipTests
echo "Did it succeed?"
read -p "Press enter to continue"

cd ../vscode
newVscVer=`cat package.json  | grep "\"version\":" | cut -f 2 -d ":" | sed 's/"//g' | sed 's/,//g' | awk '{$1=$1};1'`
newVscLastSegment=`echo $newVscVer | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
newVscPrefix=`echo $newVscVer | cut -f 1,2 -d "."`
newVsc=$newVscPrefix.$newVscLastSegment
echo "New version is $newVsc"

echo "Updating package.json with new version"
cat package.json | sed "s/\"version\": \"$newVscVer\",/\"version\": \"$newVsc\",/g" > package2
mv package2 package.json


echo "Committing and pushing to $curBranch"
git commit -a -m "Upversion to $newVsc - Development Begins" --signoff

if [ "$debug" -eq 0 ]; then
	git push origin $curBranch
else 
	echo git push origin $curBranch
fi

