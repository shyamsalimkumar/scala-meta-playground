package io.github.shyamsalimkumar.playground.scalameta.macros

import scala.collection.immutable.Seq
import scala.meta.Ctor.Primary
import scala.meta.Defn.Val
import scala.meta.Term.Param.Name
import scala.meta._

class Field(fieldName: String) extends scala.annotation.StaticAnnotation

class WithField extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case Term.Block(Seq(cls @ Defn.Class(_, _, _, ctor, _), companion: Defn.Object)) =>
        val validParamNames = WithFieldHelper.parseValidFieldNames(ctor)
        val validFields = WithFieldHelper.buildFieldStatements(validParamNames)
        val fieldsObject = q"object Fields { ..$validFields }"

        val statements = companion.templ.stats match {
          case Some(stats) =>
            val otherFields = stats.filter {
              case q"object Fields { ..$_ }" => false
              case _ => true
            }
            val newFieldsStatements = stats.find {
              case q"object Fields { ..$_ }" => true
              case _ => false
            }.map {
              case q"object Fields { ..$fieldStatements }" =>
                fieldStatements ++ validFields
            }
            val newFieldsObject = newFieldsStatements.map { x =>
              q"object Fields { ..$x }"
            }.getOrElse(fieldsObject)

            Seq(newFieldsObject) ++ otherFields
          case None =>
            List(fieldsObject)
        }
        val templateStats = statements
        val newCompanion = companion.copy(
          templ = companion.templ.copy(
            stats = Option(templateStats)))
        println(newCompanion.toString())
        Term.Block(Seq(cls, newCompanion))
      case cls @ Defn.Class(_, name, _, ctor, _) =>
        val validParamNames = WithFieldHelper.parseValidFieldNames(ctor)
        val validFields = WithFieldHelper.buildFieldStatements(validParamNames)
        val fieldObject = q"object Fields { ..$validFields }"
        val companion = q"object ${Term.Name(name.value)} { $fieldObject }"

        Term.Block(Seq(cls, companion))
      case _ => abort(s"@WithField must annotate a class")
    }
  }
}

object WithFieldHelper {
  def parseValidFieldNames(ctor: Primary): Seq[(Name, String)] = {
    ctor.paramss match {
      case firstParamList :: _ =>
        firstParamList.map { x =>
          val mods = x.mods
          if (mods.nonEmpty) {
            val fieldName = mods match {
              case h :: _ =>
                h match {
                  case mod"${Mod.Annot(Term.Apply(_, args))}" if args.nonEmpty =>
                    args.headOption.map(_.toString().replaceAll("\"", "")) // ToDo: Find a better way to get the string representation
                  case _ => None
                }
              case Nil =>
                None
            }
            (x.name, fieldName.getOrElse(x.name.value))
          } else {
            (x.name, x.name.value)
          }
        }
      case Nil => Nil
    }
  }

  def buildFieldStatements(validParamNames: Seq[(Name, String)]): Seq[Val] = {
    validParamNames.map { case ((term, value)) =>
      q"final val ${Pat.Var.Term(Term.Name(term.value))} = ${Lit(Helper.convertCamelToSnakeCase(value))}"
    }
  }
}
