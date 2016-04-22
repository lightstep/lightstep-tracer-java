// A little hacky, but so concise:
if (!process.env.ANDROID_HOME) {
    process.env.ANDROID_HOME = process.env.HOME + "/Library/Android/sdk";
}

build.task("build")
    .deps(["copy-sources"])
    .commands(["cd SampleApp/tracer && gradle assemble"]);

build.task("copy-sources")
    .describe("copy the generic java sources in the android folder")
    .commands({
        env: {
            "ANDROID_DEST_BASE": "android/SampleApp/tracer/src/main/java/com/lightstep",
        },
        cmds: [
            "cd .. && " +
            "mkdir -p $ANDROID_DEST_BASE/tracer && " +
            "cp -R java/src/com/lightstep/tracer/. $ANDROID_DEST_BASE/tracer",
        ],
    });

build.task("clean")
    .commands([
        "cd tracer && gradle clean && rm -rf .gradle",
    ]);

build.task("publish")
    .deps(["inc-version"])
    .commands([
        // Clone a fresh copy of the api-android git repository.
        "rm -rf /tmp/api-android",
        // XXX new github repo?
        "git clone 'git@github.com:traceguide/api-android.git' /tmp/api-android",
        "cp -rf tracer /tmp/api-android",

        // Swap the current version number into the gradle.properties file.
        ("sed -i '' " +
         "-e \"s/VERSION_SUBSTITUTION_TARGET/`cat SampleApp/tracer/VERSION`/\" " +
         "/tmp/api-android/tracer/gradle.properties"),

        // Enter the api-android repo and push a new tag to github.
        ("cd /tmp/api-android && " +
         "git add . && git commit -m \"pod version `cat SampleApp/tracer/VERSION`\" && " +
         "git tag `cat SampleApp/tracer/VERSION` && " +
         "git push && git push --tags"),

        // Use gradle to actually upload new artifacts to Sonatype/OSSRH.
        "cd /tmp/api-android/tracer && gradle uploadArchives",

        // "Educate the user"
        ("echo '\n\n\n" +
         "  YOU ARE NOT DONE!\n" +
         "\n" +
         "  1) Go to https://oss.sonatype.org/\n" +
         "  2) Login (ask bhs)\n" +
         "  3) Go to the \"Staging Repositories\" area\n" +
         "  4) Find the LightStep staging artifact\n" +
         "  5) \"Close\" it and wait 30 seconds\n" +
         "  6) \"Release\" it and wait 30 minutes (!!)\n" +
         "  7) Your new version should be in both the jcenter and\n" +
         "     Maven Central repos\n\n'"),
    ]);

build.task("inc-version")
    .describe("bumps the version number for the android Maven artifact")
    .commands([
        ("awk 'BEGIN { FS = \".\" }; { printf(\"%d.%d.%d\", $1, $2, $3+1) }' " + 
         "SampleApp/tracer/VERSION > " +
         "SampleApp/tracer/VERSION.incr"),
        ("mv SampleApp/tracer/VERSION.incr SampleApp/tracer/VERSION"),
    ]);
