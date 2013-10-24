#!/bin/sh

for INPUT in *.svg; do 
inkscape -w 48 -e ../res/drawable-mdpi/ic_home.png  $INPUT;
inkscape -w 144 -e ../res/drawable-xxhdpi/ic_home.png  $INPUT;
inkscape -w 72 -e ../res/drawable-hdpi/ic_home.png  $INPUT;
inkscape -w 96 -e ../res/drawable-xhdpi/ic_home.png  $INPUT;
done




