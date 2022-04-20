package io.gitlab.mateuszjaje.jiraclient
package marshalling

import io.circe.{Codec, Decoder, Encoder}

import scala.reflect.ClassTag

object EnumMarshalling {
  def stringEnumCodecFor[A: ClassTag](possibleValues: String => Option[A])(encode: A => String): Codec[A] = {
    val decoder: Decoder[A] = Decoder.decodeString.emap { rawValue =>
      possibleValues(rawValue)
        .toRight(s"$rawValue is not valid one for ${implicitly[ClassTag[A]].runtimeClass.getCanonicalName}")
    }
    val encoder: Encoder[A] = Encoder.encodeString.contramap[A](encode)
    Codec.from(decoder, encoder)
  }

  def stringEnumCodecFor[A: ClassTag](possibleValues: Seq[A])(encode: A => String): Codec[A] =
    stringEnumCodecFor[A](possibleValues.map(x => encode(x) -> x).toMap.get _)(encode)

  def stringEnumCodecOf[A: ClassTag](handler: EnumMarshallingGlue[A]): Codec[A] =
    stringEnumCodecFor[A](handler.byName.get _)(handler.rawValue)

  def safeStringEnumCodecOf[A: ClassTag](handler: EnumMarshallingGlue[A], default: String => A): Codec[A] =
    stringEnumCodecFor[A]((x: String) => handler.byName.get(x).orElse(Some(default(x))))(handler.rawValue)
}

trait EnumMarshallingGlue[T] {
  def rawValue: T => String

  def byName: Map[String, T]
}
