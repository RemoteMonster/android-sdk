[![JitPack](https://jitpack.io/v/com.gitlab.kakao-i-connect-live/android-sdk.svg)](https://jitpack.io/#com.gitlab.kakao-i-connect-live/android-sdk)

# RemoteMonster Android SDK

RemoteMonster - Livecast Management in the Cloud
* [Website](https://remotemonster.com)


## Get SDK

### Package Manager
```project:build.gradle
// build.gradle(Project:root)

allprojects {
    repositories {
    	....
	maven { url 'https://icl.jfrog.io/artifactory/kakaoenterprise' }
    }
}
```

```module:build.gradle
// build.gradle(Module:app)

dependencies {
    implementation 'com.kakaoenterprise:kakao-i-connect-live:2.9.13'
}
```

### Downloads
* [Downloads](https://github.com/RemoteMonster/android-sdk/releases/)


## Examples

### SimpleCall `Kotlin`
* [source](https://github.com/RemoteMonster/android-sdk/tree/master/examples/SimpleCall)

### Full features `Java`
* [source](https://github.com/RemoteMonster/android-sdk/tree/master/examples/full/)

### Simple Conference `Kotlin`
* [source](https://github.com/RemoteMonster/android-sdk/tree/master/examples/SimpleConference)
* service reference : 와글 투게더
    * [android](https://play.google.com/store/apps/details?id=com.remotemonster.waggletogether)
    * [web](https://waggle.live/)

## Documents

* [Guides](https://docs.remotemonster.com/)
* [API Reference](https://remotemonster.github.io/android-sdk/)



## Changelog
* [Changelog](https://github.com/RemoteMonster/android-sdk/blob/master/CHANGELOG.md)
