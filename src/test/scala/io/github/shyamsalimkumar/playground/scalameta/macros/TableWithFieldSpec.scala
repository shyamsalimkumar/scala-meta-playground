package io.github.shyamsalimkumar.playground.scalameta.macros

import org.scalatest._

class TableWithFieldSpec extends FlatSpec with Matchers {
  "@Table@WithField" should "compile successfully if annotated on a class" in {
    assertCompiles(
      """
        |@Table
        |    @WithField
        |    class Foo(a: String, b: Long)
      """.stripMargin)
  }

  it should "compile successfuly if companion object is present" in {
    assertCompiles(
      """
        |@Table
        |    @WithField
        |    class Foo(a: String, b: Long)
        |
        |    object Foo {
        |      def someFancyMethod: String = "a"
        |    }
      """.stripMargin)
  }

  it should "compile successfully if @Field annotations are present" in {
    assertCompiles(
      """
        |@Table
        |    @WithField
        |    class Foo(@Field("funky_name") a: String, b: Long)
        |
        |    object Foo {
        |      def someFancyMethod: String = "a"
        |    }
      """.stripMargin)
  }

  it should "have appropriate fields" in {
    {
      @Table
      @WithField
      class Foo(a: String, b: Long)

      assert(Foo.tableName == "foo")
      assert(Foo.Fields.a == "a")
      assert(Foo.Fields.b == "b")
    }

    {
      @Table
      @WithField
      class Foo(a: String, b: Long)

      object Foo {
        def someFancyMethod: String = "a"
      }

      assert(Foo.tableName == "foo")
      assert(Foo.Fields.a == "a")
      assert(Foo.Fields.b == "b")
//      assert(Foo.someFancyMethod = "a")
    }

    {
      @Table("a_table")
      @WithField
      class Foo(a: String, b: Long)

      object Foo {
        def someFancyMethod: String = "a"
      }

      assert(Foo.tableName == "a_table")
      assert(Foo.Fields.a == "a")
      assert(Foo.Fields.b == "b")
//      assert(Foo.someFancyMethod = "a")
    }

    {
      @Table("a_table")
      @WithField
      class Foo(@Field("funky_name") a: String, b: Long)

      object Foo {
        def someFancyMethod: String = "a"
      }

      assert(Foo.tableName == "a_table")
      assert(Foo.Fields.a == "funky_name")
      assert(Foo.Fields.b == "b")
//      assert(Foo.someFancyMethod = "a")
    }

    {
      @Table("a_table")
      @WithField
      class Foo(@Field("funky_name") a: String, @Field("funky_name2")b: Long)

      object Foo {
        def someFancyMethod: String = "a"
      }

      """
        |Generated in @Table
        |object Foo {
        |  final val tableName = "a_table"
        |  def someFancyMethod: String = "a"
        |}
        |
        |Generated in @WithField
        |object Foo {
        |  object Fields {
        |    final val a = "funky_name"
        |    final val b = "funky_name2"
        |  }
        |  final val tableName = "a_table"
        |  def someFancyMethod: String = "a"
        |}
      """.stripMargin

      assert(Foo.tableName == "a_table")
      assert(Foo.Fields.a == "funky_name")
      assert(Foo.Fields.b == "funky_name2")
//      assert(Foo.someFancyMethod = "a") // error: value someFancyMethod_= is not a member of object Foo
    }

    {
      @Table("a_table")
      @WithField
      class Foo(@Field("funky_name") a: String, @Field("funky_name2")b: Long)

      object Foo {
        def someFancyMethod(): String = "a"
      }

      """
        |Generated in @Table
        |object Foo {
        |  final val tableName = "a_table"
        |  def someFancyMethod(): String = "a"
        |}
        |
        |Generated in @WithField
        |object Foo {
        |  object Fields {
        |    final val a = "funky_name"
        |    final val b = "funky_name2"
        |  }
        |  final val tableName = "a_table"
        |  def someFancyMethod(): String = "a"
        |}
      """.stripMargin

      assert(Foo.tableName == "a_table")
      assert(Foo.Fields.a == "funky_name")
      assert(Foo.Fields.b == "funky_name2")
//      assert(Foo.someFancyMethod() = "a") // error: value update is not a member of String
    }
  }
}
