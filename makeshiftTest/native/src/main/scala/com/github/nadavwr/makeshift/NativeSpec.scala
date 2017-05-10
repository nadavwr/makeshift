package com.github.nadavwr.makeshift

import scalanative.native._

//noinspection TypeAnnotation
object NativeSpec extends App with SharedSpec {

  class Buffer(val size: Int) {
    val ptr: Ptr[Byte] = stdlib.malloc(1024)
    def dispose(): Unit = stdlib.free(ptr)

    def put(bytes: Array[Byte]): Unit =
      bytes.zipWithIndex.foreach { case (b, i) => !(ptr+i) = b }
    def get(size: Int): Array[Byte] = {
      val out = new Array[Byte](size)
      for (i <- 0 until size) {
        out(i) = !(ptr+i)
      }
      out
    }
  }

  trait BufferFixture extends Fixture {
    lazy val buffer = new Buffer(1024).withCleanup("bufer") { _.dispose() }
  }

  test("manipulate heap memory") runWith new BufferFixture {
    val message = "hello"
    val messageBytes = message.toArray.map(_.toByte)
    buffer.put(messageBytes)
    val output = buffer.get(messageBytes.length)
    assertThat(output sameElements messageBytes,
      s"buffer should be assigned expected value '$message'")
  }

  test("direct object memory access") runWith new Fixture {
    val nesting = 2 // for nested (_<:Fixture)#Foo; 1 for top-level class
    class Foo(var a: Int) {
      def toPtr: Ptr[Foo] = this.asInstanceOf[_Object].cast[Ptr[Foo]]
    }
    object Foo {
      def fromPtr(fooPtr: Ptr[Foo]): Foo = fooPtr.cast[_Object].asInstanceOf[Foo]
    }
    val foo = new Foo(0)

    val fooPtr = foo.toPtr
    val headerSize = sizeof[Ptr[scalanative.runtime.Type]]*nesting
    val aPtr = (fooPtr.cast[Ptr[Byte]] + headerSize).cast[Ptr[CInt]]

    assertThat(Foo.fromPtr(fooPtr).a == 0, "Ptr[Foo] should be recast as Foo")
    assertThat(!aPtr == 0, "fooPtr->a should be assigned 0")
    foo.a = Int.MaxValue
    assertThat(!aPtr == Int.MaxValue, "fooPtr->a should be assigned MAX_INT")
    foo.a = Int.MinValue
    assertThat(!aPtr == Int.MinValue, "fooPtr->a should be assigned MIN_INT")
    stdio.fflush(stdio.stdout);
  }
}

