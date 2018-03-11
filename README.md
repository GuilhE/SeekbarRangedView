# SeekBarRangedView:
[![Build Status](https://travis-ci.org/GuilhE/android-seekbar-ranged-view.svg?branch=master)](https://travis-ci.org/GuilhE/android-seekbar-ranged-view)  [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SeekBarRangedView-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/6115)

A SeekBar restrained by a minimum and maximum value.

Back in 2014 I contributed to this [project](https://github.com/GuilhE/android-nickaknudson/commits/master) by adding a few functionalities to ___RangeSeekBar.java___. The repo had no activity since then, so I've decided to extend it and continue.
Credits must be shared with [Nick Knudson](https://github.com/nickaknudson) ;)

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
<com.github.guilhe.rangeseekbar.SeekBarRangedView
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

![example](sample1.png)

otherwise you'll be adding "margins" to your view (ex, `android:layout_height="200dp"`):

![example](sample2.png)

If you want to change its height, use the `backgroundHeight` and/or `progressHeight` attributes instead:

![example](sample3.png)


To customize this `View` by code, these are the available methods to do so:
```java
    public interface OnSeekBarRangedChangeListener {
        void onChanged(SeekBarRangedView view, float minValue, float maxValue);

        void onChanging(SeekBarRangedView view, float minValue, float maxValue);
    }
    
    public void setSeekBarRangedChangeListener(OnSeekBarRangedChangeListener listener) {}

    public float getMinValue() {}

    public void setMinValue(float value) {}
        
    public float getMaxValue() {}
    
    public void setMaxValue(float value) {}
    
    public void setSelectedMinValue(float value) {}

    public void setSelectedMinValue(float value, boolean animate) {}

    public void setSelectedMinValue(float value, boolean animate, long duration) {}

    public float getSelectedMinValue() {}

    public void setSelectedMaxValue(float value) {}

    public void setSelectedMaxValue(float value, boolean animate) {}

    public void setSelectedMaxValue(float value, boolean animate, long duration) {}

    public float getSelectedMaxValue() {}

    public void setRounded(boolean rounded) {}

    public void setBackgroundHeight(float height) {}

    public void setProgressHeight(float height) {}
        
    public void setBackgroundColor(int red, int green, int blue) {}

    public void setBackgroundColor(int alpha, int red, int green, int blue) {}

    public void setBackgroundColor(Color color) {}

    public void setBackgroundColor(int color) {}

    public void setBackgroundColorResource(@ColorRes int resId) {}

    public void setProgressColor(int red, int green, int blue) {}

    public void setProgressColor(int alpha, int red, int green, int blue) {}

    public void setProgressColor(Color color) {}

    public void setProgressColor(int color) {}

    public void setProgressColorResource(@ColorRes int resId) {}

    public void setThumbsImage(Bitmap bitmap) {}

    public void setThumbsImageResource(@DrawableRes int resId) {}

    public void setThumbNormalImage(Bitmap bitmap) {}

    public void setThumbNormalImageResource(@DrawableRes int resId) {}

    public void setThumbPressedImage(Bitmap bitmap) {}

    public void setThumbPressedImageResource(@DrawableRes int resId) {}
```

For more details checkout _javadocs_ or the code itself.

![example](sample.gif)
 

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
