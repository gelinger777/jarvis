package com.tars.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OptionTest {

  @Test
  public void testOf() {
    assertEquals(Option.of("value").get(), "value");
  }

  @Test(expected = NullPointerException.class)
  public void testOfIllegal() {
    Option.of(null);
  }

  @Test
  public void testOfNullable() {
    Option<String> opt = Option.ofNullable("value");

    assertNotNull(opt);
    assertEquals(opt.get(), "value");

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
        .ifPresent((value) -> assertTrue(true))
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
        .ifPresent((value) -> assertTrue(value == 0))
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testFlatMap() {
    Option.of("value")
        .flatMap(s -> Option.of(s.length()))
        .ifPresent((value) -> assertTrue(value == 5))
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testOrElse() {
    Option.empty()
        .ifNotPresentTake("value")
        .ifPresent((value) -> assertEquals(value, "value"))
        .ifNotPresentThrow(IllegalStateException::new);
  }

  @Test
  public void testOrElseGet() {
    Option.empty()
        .ifNotPresentTake(() -> "value")
        .ifPresent((value) -> assertEquals(value, "value"))
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

  public static void main(String[] args) {
//    long millisecondsSinceEpoch = System.currentTimeMillis();
//
//    DateTimeFormatter.ofLocalizedTime (FormatStyle.SHORT).format(Instant.ofEpochMilli (millisecondsSinceEpoch ));
//
//
//
//    DateTimeFormatter formatter =
//    String output = formatter.format ( zdt );
//
//    System.out.println ( "millisecondsSinceEpoch: " + millisecondsSinceEpoch + " instant: " + instant + " output: " + output );
  }
}