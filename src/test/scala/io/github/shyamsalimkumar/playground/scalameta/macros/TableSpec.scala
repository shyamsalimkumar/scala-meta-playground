package io.github.shyamsalimkumar.playground.scalameta.macros

import org.scalatest._

class TableSpec extends FlatSpec with Matchers {
  "@Table" should "compile successfully if table name is present (no companion object)" in {
    assertCompiles(
      """@Table("fake_table")
        |class A(a: String, b: Int)""".stripMargin)
  }

  it should "compile successfully if table name is present (with companion object)" in {
    assertCompiles("""@Table("fake_table")
                     |    class A(a: String, b: Int)
                     |
                     |    object A {
                     |      def time = System.currentTimeMillis()
                     |      object Fields {
                     |        final val a = "a"
                     |      }
                     |    }""".stripMargin)
  }

  it should "compile successfully if no table name is present" in {
    assertCompiles(
      """@Table
        |class A(a: String)""".stripMargin)
  }

  it should "fail compilation if annotated on an object" in {
    assertDoesNotCompile(
      """
        |@Table
        |    object ABCD { }
      """.stripMargin)
  }

  it should "have tableName" in {
    {
      @Table("fake_table")
      class A(a: String, b: Int)

      object A {
        def dummyMethod = "asd"
      }

      assert(A.tableName == "fake_table")
      assert(A.dummyMethod == "asd")
    }

    {
      @Table("asd")
      class A(a: String)

      assert(A.tableName == "asd")
    }

    {
      @Table
      class A(a: String)

      assert(A.tableName == "a")
    }

    {
      @Table
      class MyFancyClass(a: String)

      assert(MyFancyClass.tableName == "my_fancy_class")
    }
  }

  it should "abort if tableName is already declared in companion object" in {
    {
      assertDoesNotCompile(
        """
          |@Table("fake_table")
          |      class A(a: String, b: Int)
          |
          |      object A {
          |        val tableName = "goofy"
          |      }
        """.stripMargin)
    }

    {
      assertDoesNotCompile(
        """
          |@Table("fake_table")
          |      class A(a: String, b: Int)
          |
          |      object A {
          |        def tableName = "goofy"
          |      }
        """.stripMargin)
    }
  }
}
