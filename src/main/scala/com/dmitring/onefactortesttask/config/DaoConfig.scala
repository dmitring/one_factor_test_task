package com.dmitring.onefactortesttask.config

import com.typesafe.config.Config

case class DaoConfig(
  userLabelsFilePath: String,
  mappingFilePath: String
)

object DaoConfig {
  def apply(root: Config): DaoConfig = DaoConfig(
    userLabelsFilePath = root.getString("user-file-path"),
    mappingFilePath = root.getString("mapping-file-path")
  )
}
