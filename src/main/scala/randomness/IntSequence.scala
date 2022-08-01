package com.selfdualbrain.continuum.randomness

import scala.util.Random

object IntSequence extends NumericSequencesModule[Int](coerce = (x: Double) => x.toInt, nextRandomValue = (random: Random, range: Int) => random.nextInt(range)) {
}
