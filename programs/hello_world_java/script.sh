#!/bin/bash

adb uninstall com.android.helloworldjava
ant release
ant installr
adb shell reboot
sleep 120

for i in {1..10}
do
adb shell am start -n com.android.helloworldjava/.HelloWorld
sleep 5
adb shell am force-stop com.android.helloworldjava
sleep 5
done

adb logcat -d -v time > logcat
adb shell getprop > getprop
