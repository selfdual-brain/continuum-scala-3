package com.selfdualbrain.continuum
package network

trait BinarySizeIntrospectionForMessages[M] {
  def binarySizeOf(msg: M): Int
}
