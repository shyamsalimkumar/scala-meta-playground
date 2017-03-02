package io.github.shyamsalimkumar.playground.scalameta.macros

import org.scalatest._

class WithFieldSpec extends FlatSpec with Matchers {
  "@WithField" should "compile successfully if annotated on a class" in {
    assertCompiles(
      """
        |@WithField
        |    class A(a: String, b: Int)
      """.stripMargin)
  }

  it should "compile successfully if companion object is present" in {
    assertCompiles(
      """
        |@WithField
        |    class A(a: String, b: Int)
        |
        |    object A {}
      """.stripMargin)
  }

  it should "compile successfully if companion object has Fields object declared" in {
    assertCompiles(
      """
        |@WithField
        |    class A()
        |
        |    object A {
        |      object Fields {
        |
        |      }
        |    }
      """.stripMargin)
  }

  it should "fail compilation if annotated on an object" in {
    assertDoesNotCompile(
      """
        |@WithField
        |    object A { }
      """.stripMargin)
  }

  it should "contain the params of the class constructor in the Field object of the companion object" in {
    @WithField
    class A(firstField: String, b: Int)

    assert(A.Fields.firstField == "first_field")
    assert(A.Fields.b == "b")
  }

  it should "contain overwritten params of the class constructor" in {
    {
      @WithField
      class A(@Field("first") firstField: String, b: Int)

      assert(A.Fields.firstField == "first")
      assert(A.Fields.b == "b")
    }

    {
      @WithField
      class A(@Field("first") firstField: String, @Field("bad_name") b: Int)

      assert(A.Fields.firstField == "first")
      assert(A.Fields.b == "bad_name")
    }

    {
      @WithField
      class A(@Field("first") firstField: String, @Field("bad_name") b: Int)

      object A {
        object Fields {
          def time = 1
        }
        def dummyMethod = "add"
      }

      assert(A.Fields.firstField == "first")
      assert(A.Fields.b == "bad_name")
      assert(A.Fields.time == 1)
      assert(A.dummyMethod == "add")
    }
  }

  it should "fail compilation if annotated values are present in Fields object" in {
//    pending
    assertDoesNotCompile(
      """
        |@WithField
        |    class A(@Field("first") firstField: String, @Field("bad_name") b: Int)
        |
        |    object A {
        |      object Fields {
        |        final val b = "bad_field"
        |      }
        |    }
      """.stripMargin)

    assertDoesNotCompile(
      """
        |@WithField
        |    class A(@Field("first") firstField: String, @Field("bad_name") b: Int)
        |
        |    object A {
        |      object Fields {
        |        def b = "bad_field"
        |      }
        |    }
      """.stripMargin)
  }
}
