package pl.piszkod.cinema.service

import munit.CatsEffectSuite

class ScheduleManagerSpec extends CatsEffectSuite {

  test("Should get full schedule") {}

  test("Should schedule slot") {}

  test("Should fail if tries to schedule premiere show for wrong hours") {}

  // TODO: How to test?
  test("Should not allow for concurrent schedule modifications") {}

  test("Should fail when new slot collides with existing one") {}

}
