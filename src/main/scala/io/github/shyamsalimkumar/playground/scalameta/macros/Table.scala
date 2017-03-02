package io.github.shyamsalimkumar.playground.scalameta.macros

import scala.collection.immutable.Seq
import scala.meta._

class Table(tableName: String) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val maybeTableName = this match {
      case q"new $_(${Lit(tableName: String)})" => Option(tableName)
      case _ => None
    }

    defn match {
      case Term.Block(Seq(cls @ Defn.Class(_, name, _, _, _), companion: Defn.Object)) =>
        val tableName = maybeTableName match {
          case Some(t) => t
          case None => Helper.convertCamelToSnakeCase(name.value)
        }
        val tableNameFinalVal = q"final val tableName = ${Lit(tableName)}"
        val templateStats = tableNameFinalVal +: companion.templ.stats.getOrElse(Nil)
        val newCompanion = companion.copy(
          templ = companion.templ.copy(
            stats = Option(templateStats)))
        println("=" * 10)
        println(newCompanion.toString())
        Term.Block(Seq(cls, newCompanion))
      case cls @ Defn.Class(_, name, _, _, _) =>
        val tableName = maybeTableName match {
          case Some(t) => t
          case None => Helper.convertCamelToSnakeCase(name.value)
        }
        val tableNameFinalVal = q"final val tableName = ${Lit(tableName)}"
        val companion = q"object ${Term.Name(name.value)} { $tableNameFinalVal }"

        Term.Block(Seq(cls, companion))
      case _ => abort(s"@Table must annotate a class")
    }
  }
}
