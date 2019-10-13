# SeekBarRangedView
[![Build Status](https://travis-ci.org/GuilhE/android-seekbar-ranged-view.svg?branch=master)](https://travis-ci.org/GuilhE/android-seekbar-ranged-view)  [![Codacy Badge](https://api.codacy.com/project/badge/Grade/3cc30ca7af394140983f6fcfdfc98c83)](https://www.codacy.com/app/GuilhE/android-seekbar-ranged-view?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=GuilhE/android-seekbar-ranged-view&amp;utm_campaign=Badge_Grade)  [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SeekBarRangedView-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/6115)

A SeekBar restrained by a minimum and maximum value.

Back in 2014 I contributed to this [project](https://github.com/GuilhE/android-nickaknudson/commits/master) by adding a few functionalities to ___RangeSeekBar.java___. The repo had no activity since then, so I've decided to extend it and continue.
Credits must be shared with [Nick Knudson](https://github.com/nickaknudson) ;)

#### Version 2.x
- **October, 2019** - Thumbs by drawable better support
- **August, 2019** - Progress-by-Step added

<img src="sample4.png" alt="Sample4" width="30%"/>
<img src="sample5.png" alt="Sample5" width="30%"/>

#### Version 1.x
- **March, 2018** - Programmatically change max and min values
- **August, 2017** - SeekBarRangedView


## Getting started

Include it into your project, for example, as a Gradle compile dependency:

```groovy
compile 'com.github.guilhe:seekbar-ranged-view:${LATEST_VERSION}'
```
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.guilhe/seekbar-ranged-view/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22seekbar-ranged-view%22)  [ ![Download](https://api.bintray.com/packages/gdelgado/android/seekbar-ranged-view/images/download.svg) ](https://bintray.com/gdelgado/android/seekbar-ranged-view/_latestVersion)  

## Sample usage

Check out the __sample__ module where you can find a few examples of how to create it by `xml` or `java`.

Attributes accepted in xml:
```xml
<declare-styleable name="SeekBarRangedView">
        <attr name="min" format="float"/>
        <attr name="max" format="float"/>
        <attr name="currentMin" format="float"/>
        <attr name="currentMax" format="float"/>
        <attr name="rounded" format="boolean"/>
        <attr name="backgroundColor" format="color"/>
        <attr name="backgroundHeight" format="dimension"/>
        <attr name="progressColor" format="color"/>
        <attr name="progressHeight" format="dimension"/>
        <attr name="thumbsResource" format="reference"/>
        <attr name="thumbNormalResource" format="reference"/>
        <attr name="thumbPressedResource" format="reference"/>
 </declare-styleable>
```
Example:
```xml
<com.github.guilhe.views.SeekBarRangedView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                custom:currentMin="10"
                custom:backgroundColor="#C0C0C0"
                custom:progressColor="@color/progress_bar_line"
                custom:backgroundHeight="10dp"
                custom:progressHeight="20dp"
                custom:rounded="true"/>
 ```

For `android:layout_height` you should use `"wrap_content"`:

<img src="sample1.png" alt="Sample1" width="50%"/>

otherwise you'll be adding "margins" to your view (ex, `android:layout_height="200dp"`):

<img src="sample2.png" alt="Sample2" width="50%"/>

If you want to change its height, use the `backgroundHeight` and/or `progressHeight` attributes instead:

<img src="sample3.png" alt="Sample3" width="50%"/>


There are many methods to help you customize this `View` by code. For more details checkout the __sample app__, _javadocs_ or the code itself.

## Sample
<img src="sample.gif" alt="Sample" width="30%"/>

_Animation last update on November, 2018_

<a href='https://play.google.com/store/apps/details?id=com.github.guilhe.rangeseekbar.sample&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img width="30%" alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>
 

## Binaries

Binaries and dependency information for Gradle, Maven, Ivy and others can be found at [https://search.maven.org](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22seekbar-ranged-view%22).

<a href='https://bintray.com/gdelgado/android/seekbar-ranged-view?source=watch' alt='Get automatic notifications about new "seekbar-ranged-view" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_bw.png'></a>

## Dependencies

- [com.android.support:support-annotations](https://developer.android.com/topic/libraries/support-library/packages.html#annotations)

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/GuilhE/android-seekbar-ranged-view/issues).

 
## LICENSE

Copyright (c) 2017-present, SeekBarRangedView Contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
