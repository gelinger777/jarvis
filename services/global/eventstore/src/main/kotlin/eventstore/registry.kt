package eventstore

import java.nio.file.Path

internal object registry {

    var rootDir = "temp" // todo replace with actual system temp

    fun setRootDir(path: Path){
        rootDir = path.toAbsolutePath().toString()
    }



}