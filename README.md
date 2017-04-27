A unit testing library I quickly hacked together for personal use, until one of the established libraries becomes available for Scala Native.

To use it:

```scala
resolvers += Resolver.bintrayRepo("nadavwr", "maven")
libraryDependencies += "com.github.nadavwr" %%% "makeshift" % "0.1.0"
```

* Unit tests are aggregated under `Spec` instances.
* Each unit test makes use of a `Fixture`
* Using fixtures enables sharing code across test cases, as well as orderly resource initialization and cleanup.
* The method `assertThat()` provides some rudimentary papertrail for what specific checks pass/fail. Make sure your terminal can render 👍 and ❗.
* No effort is made by the library to aggregate specs into test suites. Having a `Spec` extend `App` has been a guilty pleasure for me so far.
* SBT test framework integration isn't there yet, so just place your specs in a separate module and plain-old `run` them.

Given a heap-allocated buffer such as the following:

```scala
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
```

It can be tested as seen below:

```scala
import com.github.nadavwr.makeshift._

object MySpec extends Spec with App {

  trait BufferFixture extends Fixture {
    lazy val buffer = new Buffer(1024)
      .withCleanup("buffer") { _.dispose() }
  }

  test("manipulate heap memory") runWith new BufferFixture {
    val message = "hello"
    val messageBytes = message.toArray.map(_.toByte)
    buffer.put(messageBytes)
    val output = buffer.get(messageBytes.length)
    assertThat(output sameElements messageBytes,
      s"buffer should be assigned expected value '$message'")
  }
}
```

producing the following output:
```
_______________________
▏manipulate heap memory
bufer created
👍 buffer should be assigned expected value 'hello'
👍
cleaning up bufer
```

See more usage examples in [SampleSpec](sample/src/main/scala/com/github/nadavwr/makeshift/SampleSpec.scala)

