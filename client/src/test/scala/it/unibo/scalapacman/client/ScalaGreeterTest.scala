package it.unibo.scalapacman.client

import org.scalatest.wordspec.AnyWordSpec

class ScalaGreeterTest extends AnyWordSpec {

  // Describe a scope for a subject, in this case: "A Set"
  "A Greeter" can { // All tests within these curly braces are about "A Set"

    // Can describe nested scopes that "narrow" its outer scopes
    "always" should { // All tests within these curly braces are about "A Set (when empty)"

      "greet with hello" in {    // Here, 'it' refers to "A Set (when empty)". The full name
        val res = ScalaGreeter().sayHello;
        assert((res.toLowerCase) startsWith "hello") // of this test is: "A Set (when empty) should have size 0"
      }
    }
  }
}
