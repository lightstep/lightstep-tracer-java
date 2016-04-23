# Android

**TL;DR:**

* run `rbuild build` - builds the Android instrumentation library
* `sample/SampleApp` - contains a *non-quite-yet-instrumented* sample app to test the customer integration steps against

## Developer notes (i.e. non-customer)

Required dependencies and tools:

* `brew install gradle` to install gradle
* `brew cask install java`
* Manually download and install Android Studio.
* Open Android Studio and open the SDK Manager.
    * Use the SDK Manager to install SDK version 21.
    * Use the SDK Manager to install an appropriate version of the build tools (and update `instrument/build.gradle` if necessary).
    * Install an emulator or configure your Android phone.
* Open the `tracer` directory in the Android Studio to "sync gradle versions".
* It'll also ask you to install the "build tools" after the sync.

Heads up about the build process:

* **Warning:** to simplify the build process (and make this buildable without a full copy of our repo), the build scripts *copy the generic Java instrumentation source into the `src` directory*. The true source file(s) are all under the `com/lightstep/tracer/android` directory
* **Warning:** It seems the Android emulator [does not play well with VirtualBox](http://stackoverflow.com/questions/17024538/how-do-i-fix-failed-to-sync-vcpu-reg-error). `boot2docker stop` was needed for bc to get the Android emulator to stop silently failing
* Android Studio *caches* a lot of configuration - there's an option to invalidate caches and restart Andriod Studio in the File menu. Use it to avoid headaches after any git file reverts.
* Worth noting: Android builds `.aar` files, which are simply `.zip` files containing the `.jar` and some additional files


#### Test Application Notes

Test application is under `/sample/SampleApp`.

## Customer integration

Ideally, eventually the integration should mirror: https://mixpanel.com/help/reference/android.  For now, it's a bit more manual.

Notes:

* "jcenter" seems to be the distribution mechanism of choice for libraries, but this seems to have a non-trivial setup and non-trivial cost for non-open source projects
* Distributing an AAR directory simply does not seem to work despite Andriod Studio having a feature that imports AARs (dependencies do not resolve correctly even after manual edits to the gradle files).
* Unclear: these steps do not account for **signing** which may prevent the built application from being distributed (?) or it is possible since the imported module is being built from source it will inherit the right signing from the host application.

## SampleApp integratino

The SampleApp already imports LightStep, so all you need to do is uncomment the code (and fix the imports).

### Work around an gradle dependencies defect:

* Open the `build.gradle` for the application module (i.e. found in the `app` directory) and within the `android` section add or update the `packagingOptions` section with:

```
    packagingOptions {
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/NOTICE.txt'
    }
```

### Finally allow internet permissions to the app if it does not already have it:

* Then ensure the app's `AndroidManifest.xml` has the following (under the `<manifest>` tag):

```xml
    <!-- Permissions required to make http calls -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
