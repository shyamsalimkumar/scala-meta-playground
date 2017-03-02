package io.github.shyamsalimkumar.playground.scalameta.macros

object Helper {
  def convertCamelToSnakeCase(s: String): String = {
    s.foldLeft("") {
      case (agg, char) =>
        if (agg.nonEmpty && char != char.toLower) {
          agg ++ "_" + char.toString.toLowerCase
        } else {
          agg + char.toLower
        }
    }
  }
}
