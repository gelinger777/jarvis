package util.common;

import org.junit.*;

import util.*;


public class OptionTestJava {

  @Test
  public void testOf() {
    Assert.assertEquals(Option.of("value").get(), "value");
  }

  @Test(expected = NullPointerException.class)
  public void testOfIllegal() {
    Option.of(null);
  }

  @Test
  public void testOfNullable() {
    Option<String> opt = Option.ofNullable("value");

    Assert.assertNotNull(opt);
    Assert.assertEquals(opt.get(), "value");

    Option.ofNullable(null);
  }

  @Test
  public void testGet() {
    assert Option.of("value").get().equals("value");
  }

  @Test
  public void testIsPresent() {
    assert Option.of("value").isPresent();
  }

  @Test
  public void testIfPresent() {
    Option.of("value")
        .ifPresent((value) -> {
          Assert.assertTrue(true);
        })
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testFilter() {
    Option.of("some text")
        .filter(s -> s.startsWith("some"))
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testMap() {
    Option.of("")
        .map(String::hashCode)
        .ifPresent((value) -> {
          Assert.assertTrue(value == 0);
        })
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testFlatMap() {
    Option.of("value")
        .flatMap(s -> Option.of(s.length()))
        .ifPresent((value) -> {
          Assert.assertTrue(value == 5);
        })
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testOrElse() {
    Option.empty()
        .ifNotPresentTake("value")
        .ifPresent((value) -> {
          Assert.assertEquals(value, "value");
        })
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testOrElseGet() {
    Option.empty()
        .ifNotPresentCompute(() -> "value")
        .ifPresent((value) -> {
          Assert.assertEquals(value, "value");
        })
        .ifNotPresentThrow(IllegalStateException::new);

  }

  @Test(expected = IllegalStateException.class)
  public void testOrElseThrow() {
    Option.empty()
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testEquals() {
    assert Option.of("value").equals(Option.of("value"));
    assert Option.ofNullable(null).equals(Option.empty());
  }

  @Test
  public void testHashCode() {
    assert Option.empty().hashCode() == 0;
  }

  @Test
  public void testToString() {
    assert Option.of("value").toString().equals("Option[value]");
  }

}