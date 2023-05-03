#!/bin/bash

# display help
_display_help() {
    echo
    echo "usage: ./notar.sh -a /path/to/app.app -u jbloggs@esri.com -p abcd-efgh-1234-5678"
    echo
    echo " -a the path to the app, including .app extension"
    echo " -u Your Apple ID (uname@esri.com). You must be part of the ESRI developer organisation."
    echo " -p App-specific password. Create online in Apple ID settings."
    echo " -h this help"
    echo
    echo " NB: if you do not have the signing certificate on your machine"
    echo " then codesigning will fail. Required - Developer ID Application: ESRI (75Z67E6SV2)"
    exit 1
}

# args are
# $1: exit code
# $2: command that failed
# will exit the script with any nonzero exit code
validate_exit_code_() {
    if [ $1 != 0 ] ; then
        echo "error:" $2 "Exit code" $1
        exit $1
    fi
}

if [ $# == 0 ] ; then
    _display_help
fi

# variables
APPFULLPATH=
USERNAME=
PASSWORD=
TEAM_ID="P8HGHS7JQ8"
# sha-1 fingerprint of Developer ID certificate
CODESIGN="C3CE5A752B28A85BDBC8F537E00D79295CC1FD7F"
# embedded XML file because gradle pretends it can't find it otherwise
ENTITLEMENTS="<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
<dict>
    <key>com.apple.security.cs.allow-jit</key>
    <true/>
    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
    <true/>
    <key>com.apple.security.cs.disable-library-validation</key>
    <true/>
    <key>com.apple.security.cs.allow-dyld-environment-variables</key>
    <true/>
</dict>
</plist>"

# parse options
while getopts :a:u:p:h opt; do
    case $opt in
    a)
        APPFULLPATH=${OPTARG}
        ;;
    u)
        USERNAME=${OPTARG}
        ;;
    p)
        PASSWORD=${OPTARG}
        ;;
    h)
        _display_help
        ;;
    \?)
        echo "Invalid option: -${OPTARG}"
        _display_help
        ;;
    esac
done

# get the app name with no directory
APP=$(basename "${APPFULLPATH}")

# cd to app's directory, which may or may not be the current directory
cd $(dirname "${APPFULLPATH}") || exit

# create an entitlements file for codesign to read
touch tmp.plist
echo "${ENTITLEMENTS}" > tmp.plist
ENTITLEMENTS=$(readlink -f tmp.plist)

## clear any quarantine attribute in case the app was copied off a network drive
xattr -d -r com.apple.quarantine "${APP}"

## Step 2
## codesign everything that moves

# sign anything that has execution permissions (IE JDK and our jnilibs)
find "${APP}" -type f -perm +111 | while read -r bin
do
    codesign --strict --force -vvv --sign "${CODESIGN}" --entitlements "${ENTITLEMENTS}" --timestamp --options runtime "${bin}"
    validate_exit_code_ $? "codesigning ${bin}"
done

# sign all other dylibs since only the ones we provide are set executable
find "${APP}" -name "*dylib" | while read -r lib
do
    codesign --strict --force -vvv --sign "${CODESIGN}" --entitlements "${ENTITLEMENTS}" --timestamp --options runtime "${lib}"
    validate_exit_code_ $? "codesigning ${lib}"
done


## Step 3
## Codesign the app itself and verify it
codesign --strict --force -vvv --sign "${CODESIGN}" --timestamp --entitlements "${ENTITLEMENTS}" --options runtime "${APP}"
codesign --verify "${APP}"

validate_exit_code_ $? "codesign --verify {APP}"

## Step 5
## Create a notarised DMG with the app inside
NAMEONLY=$(basename "${APP}" .app)

# remove any disk images that might already exist
for file in "$(pwd)"/*.dmg ; do
  rm "${file}"
done

# use hdiutil to create a read-only dmg with the notarised app inside
hdiutil create "${NAMEONLY}".readonly.dmg -fs APFS -volname "Move Graphics App" -srcfolder "${APP}"

# convert the read-only dmg into a writeable one
hdiutil convert "${NAMEONLY}".readonly.dmg -format UDRW -o "${NAMEONLY}".rw.dmg
rm "${NAMEONLY}".readonly.dmg

# attach the dmg and put a symlink to Applications in it
MOUNTPATH=$(hdiutil attach -readwrite "${NAMEONLY}".rw.dmg | grep -oE "(/Volumes/)[^CRLF]+")
pushd "${MOUNTPATH}" > /dev/null || exit
ln -s /Applications Applications
popd > /dev/null || exit

# Tweak the dmg window so our app is on the left & applications on the right
VOL_NAME=$(basename "${MOUNTPATH}")
echo '
   tell application "Finder"
     tell disk "'${VOL_NAME}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 920, 440}
           set viewOptions to the icon view options of container window
           set arrangement of viewOptions to not arranged
           set icon size of viewOptions to 72
           set position of item "'${NAMEONLY}'.app" of container window to {160, 150}
           set position of item "Applications" of container window to {360, 150}
           close
           open
           update without registering applications
           delay 2
     end tell
   end tell
' | osascript

# wait for changes to finish & eject the dmg
sync
hdiutil detach "${MOUNTPATH}"

# compress dmg before sending it off for notarisation
hdiutil convert "${NAMEONLY}".rw.dmg -format ULMO -o "${NAMEONLY}".dmg
rm "${NAMEONLY}".rw.dmg

# codesign the DMG
codesign --strict --force -vvv --sign "${CODESIGN}" --timestamp --entitlements "${ENTITLEMENTS}" "${NAMEONLY}".dmg
codesign --verify "${NAMEONLY}".dmg
validate_exit_code_ $? "codesign --verify ${NAMEONLY}.dmg"

echo "Sending DMG to Apple for notarization - this may take 5-15 minutes."
RESPONSE=$(xcrun notarytool submit --wait --apple-id ${USERNAME} --password ${PASSWORD} --team-id P8HGHS7JQ8 "${NAMEONLY}".dmg)

# Isolate the status code from the response body
STATUS=$(echo "${RESPONSE}" | grep status | awk '/status/&&!/Current status:/ {print substr($0, index($0, "status: ") + length("status: "))}')

if [[ ${STATUS} == "Accepted" ]] ; then
    echo "Notarisation successful."
else
    echo "Notarisation failed. Printing response to stdout..."
    echo "${RESPONSE}"
    ID=$(echo "${RESPONSE}" | grep id | awk '{ FS = " " } ; {print $2; exit}')
    echo "Printing log to stdout..."
    xcrun notarytool log --apple-id ${USERNAME} --password ${PASSWORD} --team-id ${TEAM_ID} "${ID}"
    exit 1
fi

# staple notarisation certificate to dmg
xcrun stapler staple "${NAMEONLY}".dmg > /dev/null
validate_exit_code_ $? "xcrun stapler staple ${NAMEONLY}.dmg"

# validate notarisation
spctl -a -t open --context context:primary-signature -v "${NAMEONLY}".dmg
validate_exit_code_ $? "spctl -a -t open --context context:primary-signature -v ${NAMEONLY}.dmg"

rm tmp.plist
