package com.github.nadavwr.makeshift

//noinspection TypeAnnotation
object SampleSpec extends App with Spec {

  object FileFixture {
    var counter: () => Int = {
      var next = 0
      () => { val result = next; next += 1; result }
    }
  }

  trait FileFixture extends Fixture {
    import java.io.File
    lazy val filename = "file" + FileFixture.counter()
    lazy val file = new File(filename)
      .withCleanup(s"File('$filename')") {
        file => if (file.exists()) file.delete()
      }
  }

  trait FileOutputStreamFixture extends FileFixture {
    import java.io.FileOutputStream
    def beforeFileCleanup(): Unit = ()
    lazy val fileOutputStream = new FileOutputStream(file)
      .withCleanup(s"FileOutputStream('$filename')") {
        fileOutputStream =>
          beforeFileCleanup()
          fileOutputStream.close()
      }
  }

  test("java.io file I/O support") runWith new FileOutputStreamFixture {
    val str = "hello"
    val strBytes = str.toArray.map(_.toByte)
    fileOutputStream.write(strBytes)

    override def beforeFileCleanup(): Unit = {
      import java.io.FileInputStream
      val fileInputStream = new FileInputStream(file)
      try {
        val content = Iterator.continually(fileInputStream.read()).takeWhile(_ != -1)
          .map(_.toByte).toArray
        assertThat(content sameElements strBytes, s"file content should match expected ('$str')")
      } finally fileInputStream.close()
    }
  }

  class Buffer(val size: Int) {
    import scalanative.native._
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
    import scala.scalanative.native._

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

