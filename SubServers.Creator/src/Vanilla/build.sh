# SubCreator Vanilla Build Script
# Usage: "bash build.sh <version> [cache]"
#
#!/usr/bin/env bash
if [ -z "$1" ]
  then
    echo ERROR: No Build Version Supplied
    rm -Rf "$0"
    exit 1
fi
function __DL() {
    if [ -x "$(command -v wget)" ]; then
        wget -O "$1" "$2"; return $?
    else
        curl -o "$1" "$2"; return $?
    fi
}
if [ -z "$2" ] || [ ! -f "$2/Vanilla-$1.jar" ]; then
    if [ -d "VanillaCord" ]; then
        rm -Rf VanillaCord
    fi
    mkdir VanillaCord
    echo Downloading the VanillaCord Launcher...
    __DL VanillaCord/VanillaCord.jar https://src.me1312.net/jenkins/job/VanillaCord/job/master/lastSuccessfulBuild/artifact/artifacts/VanillaCord.jar; __RETURN=$?
    if [ $__RETURN -eq 0 ]; then
        cd VanillaCord
        echo Launching VanillaCord
        java -jar VanillaCord.jar "$1"; __RETURN=$?;
        if [ $__RETURN -eq 0 ]; then
            echo Copying Finished Jar...
            cd ../
            if [ ! -z "$2" ] && [ -d "$2" ]; then
                cp "VanillaCord/out/$1-bungee.jar" "$2/Vanilla-$1.jar"
            fi
            cp "VanillaCord/out/$1-bungee.jar" Vanilla.jar
            echo Cleaning Up...
            rm -Rf VanillaCord
            rm -Rf "$0"
            exit 0
        else
            echo ERROR: VanillaCord exited with an error. Please try again
            rm -Rf VanillaCord
            rm -Rf "$0"
            exit 4
        fi
    else
        echo ERROR: Failed Downloading Patcher. Is Github.com down?
        rm -Rf VanillaCord
        rm -Rf "$0"
        exit 3
    fi
else
    echo Copying Cached Jar...
    cp "$2/Vanilla-$1.jar" Vanilla.jar
    echo Cleaning Up...
    rm -Rf "$0"
    exit 0
fi
exit 2