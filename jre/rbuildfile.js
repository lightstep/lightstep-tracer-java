
//
// All the work is deferred to ant -- but keep rbuild as
// an entry-point to hide some of the platform-tool differences.
//

build.task("build")
    .spawn([ "ant", "compile" ])
    .spawn([ "ant", "dist" ])
    ;
