package com.dmitring.onefactortesttask.config

import com.typesafe.config.Config

case class HttpConfig(
  host: String,
  port: Int
)

object HttpConfig {
  def apply(root: Config): HttpConfig = HttpConfig(
    host = root.getString("host"),
    port = root.getInt("port")
  )
}
