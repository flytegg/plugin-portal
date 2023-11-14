package gg.flyte.common.util

import com.google.common.hash.Hashing
import com.google.common.io.Files
import gg.flyte.common.api.plugins.schemas.HashType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun File.getSha256Hash() = Files.asByteSource(this).hash(Hashing.sha256()).toString()
fun File.getSha512Hash() = Files.asByteSource(this).hash(Hashing.sha512()).toString()

fun File.getHashes(): HashMap<HashType, String> {
    return HashMap<HashType, String>().apply {
        put(HashType.SHA256, getSha256Hash())
        put(HashType.SHA512, getSha512Hash())
    }
}