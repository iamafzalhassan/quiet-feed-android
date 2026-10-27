package com.example.quietfeed.data.remote

class ChannelNotFoundException : Exception()

class UnexpectedResponseException(val code: Int) : Exception("HTTP $code")
