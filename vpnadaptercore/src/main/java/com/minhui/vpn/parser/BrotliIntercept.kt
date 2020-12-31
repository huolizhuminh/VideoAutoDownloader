package com.minhui.vpn.parser

import okio.BufferedSource
import okio.buffer
import okio.source
import org.brotli.dec.BrotliInputStream

class BrotliIntercept : SourceIntercept{
    override fun intercept(bufferedSource: BufferedSource?): BufferedSource {
       return BrotliInputStream(bufferedSource!!.inputStream()).source().buffer()
    }
}