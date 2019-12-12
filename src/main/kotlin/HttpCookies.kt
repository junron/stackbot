/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features.cookies.mine

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.CookiesStorage
import io.ktor.client.features.cookies.mine.HttpCookies.Config
import io.ktor.client.features.feature
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.response.HttpReceivePipeline
import io.ktor.http.*
import io.ktor.util.AttributeKey
import kotlinx.io.core.Closeable
import io.ktor.client.features.cookies.mine.HttpCookies as MineHttpCookies

/**
 * [HttpClient] feature that handles sent `Cookie`, and received `Set-Cookie` headers,
 * using a specific [storage] for storing and retrieving cookies.
 *
 * You can configure the [Config.storage] and to provide [Config.default] blocks to set
 * cookies when installing.
 */
class HttpCookies(private val storage: CookiesStorage) : Closeable {

  /**
   * Find all cookies by [requestUrl].
   */
  suspend fun get(requestUrl: Url): List<Cookie> = storage.get(requestUrl)

  override fun close() {
    storage.close()
  }

  class Config {
    private val defaults = mutableListOf<CookiesStorage.() -> Unit>()

    /**
     * [CookiesStorage] that will be used at this feature.
     * By default it just uses an initially empty in-memory [AcceptAllCookiesStorage].
     */
    var storage: CookiesStorage = AcceptAllCookiesStorage()

    /**
     * Registers a [block] that will be called when the configuration is complete the specified [storage].
     * The [block] can potentially add new cookies by calling [CookiesStorage.addCookie].
     */
    fun default(block: CookiesStorage.() -> Unit) {
      defaults.add(block)
    }

    internal fun build(): MineHttpCookies {
      defaults.forEach { it.invoke(storage) }

      return MineHttpCookies(storage)
    }
  }

  companion object : HttpClientFeature<Config, MineHttpCookies> {
    override fun prepare(block: Config.() -> Unit): MineHttpCookies = Config().apply(block).build()

    override val key: AttributeKey<MineHttpCookies> = AttributeKey("HttpCookies")

    override fun install(feature: MineHttpCookies, scope: HttpClient) {
      scope.sendPipeline.intercept(HttpSendPipeline.State) {
        val cookies = feature.get(context.url.clone().build())

        with(context) {
          headers[HttpHeaders.Cookie] = renderClientCookies(cookies)
        }
      }

      scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
        val url = context.request.url
        response.setCookie().forEach {
          feature.storage.addCookie(url, it)
        }
      }
    }
  }
}

fun renderClientCookies(cookies: List<Cookie>): String = buildString {
  cookies.forEach {
    append(it.name)
    append('=')
    append(it.value.encodeURLQueryComponent())
    append(';')
    append(" ")
  }
}

/**
 * Gets all the cookies for the specified [url] for this [HttpClient].
 */
suspend fun HttpClient.cookies(url: Url): List<Cookie> = feature(MineHttpCookies)?.get(url) ?: emptyList()

/**
 * Gets all the cookies for the specified [urlString] for this [HttpClient].
 */
suspend fun HttpClient.cookies(urlString: String): List<Cookie> =
  feature(MineHttpCookies)?.get(Url(urlString)) ?: emptyList()

/**
 * Find the [Cookie] by [name]
 */
operator fun List<Cookie>.get(name: String): Cookie? = find { it.name == name }
