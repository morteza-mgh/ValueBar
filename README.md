#### ValueBar

Custom Android Valuebar view
------

![alt text](https://github.com/morteza-mgh/assets/raw/main/RangeBar-screen.png)





-------
## How to add to your project:
**Step 1. Add the JitPack repository to your build file**


Add it in your root build.gradle at the end of repositories:

```
allprojects{
		repositories {
				...
				maven { url 'https://jitpack.io' }
			}

}
```
 

**Step 2. Add the dependency to Module Gradle:**
	
```
	dependencies {
	        implementation 'com.github.morteza-mgh:ValueBar:1.0.0'
	}
```

------
## Attributes:

```
        <attr name="selectorColor" format="color"/>       *defaut is Orange
        <attr name="bgValueBarColor" format="color"/>     *default is White
        <attr name="crossLineColor" format="color"/>      *default is Gray
        <attr name="unitNameLabel" format="string" />     *default is ""
        <attr name="min" format="integer"/>               *default is 0
        <attr name="max" format="integer"/>               *default is 100
        <attr name="progress" format="integer"/>          *default is set to Min
        <attr name="popMsgTextSize" format="dimension"/>
        <attr name="barThicknessSize" format="dimension"/>
```
------
## Note:
1- You'd better not use `wrap_content` for width and height, It is highly recommneded to either specify width and height (height is 2X of width eg. `width = 150dp` & `height = 300dp`),
 or constrain layout.

2- Specify appropriate size for attr:`popMsgTextSize`
