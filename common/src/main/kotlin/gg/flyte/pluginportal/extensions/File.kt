package gg.flyte.pluginportal.extensions

import com.google.common.hash.Hashing
import com.google.common.io.Files
import gg.flyte.pluginportal.api.type.HashType
import java.io.File

fun File.getSha256Hash() = Files.asByteSource(this).hash(Hashing.sha256()).toString()
fun File.getSha512Hash() = Files.asByteSource(this).hash(Hashing.sha512()).toString()

fun File.getHashes(): HashMap<HashType, String> {
    return HashMap<HashType, String>().apply {
        put(HashType.SHA256, getSha256Hash())
        put(HashType.SHA512, getSha512Hash())
    }
}