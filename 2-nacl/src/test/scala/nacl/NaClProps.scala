package nacl

import org.abstractj.kalium.crypto.{Random, Box}
import org.abstractj.kalium.encoders.{Hex, Encoder}
import org.abstractj.kalium.keys.{KeyPair, PublicKey, PrivateKey}
import org.apache.commons.io.IOUtils
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class NaClProps extends Specification with ScalaCheck {
  "nacl.NaCl Props" should {
    "ReadWriterPing - roundtrip" in {
      // might want to do this so the key pair is also reported on failure
      implicit val arbKeyPair = Arbitrary {
        val keyPair = new KeyPair()
        (keyPair.getPrivateKey, keyPair.getPublicKey)
      }

      prop { (keyPair: (PrivateKey, PublicKey), message: String) =>
        val priv = keyPair._1.toBytes
        val pub  = keyPair._2.toBytes

        val (r, w) = NaClSpec.Pipe

        val secureR = SecureReader(r, priv, pub)
        val secureW = SecureWriter(w, priv, pub)

        secureW.write(message)
        secureW.close()

        val read = IOUtils.toString(secureR)
        read == message
      }
    }

    "ReadWriterPing counter example - encrypted length > 255" in {
      val message = """"戶揶ꂣ◳쿮亡肈ஜ刺柵䊦蒰漕楍ၹ鋳贀뱃죙惍풚蹁芈뤮ửṏ௯᡼챏Ṿ⺴磻夌ꗝ걽꩞艂襩붺䈈萐ﵓ→揭싆ﱺ쩨ᮨ鯦뚫뷣뾭체舯ଚɖⶶ鷄鑩苨튰ూ奛Ⴈન瑟ꜩ系ն젏뚹濣ખ䟌ᰀ㍱"""
      val priv = new PrivateKey("bec0456a020332235c8e1cc936632727169a9f631e4b0659cd81b04741656a67").toBytes
      val pub  = new PublicKey("9d935d507b1a5d6b319c67eab1da6a0c93b721df4d0d653e73d8ce7706442f26").toBytes

      val (r, w) = NaClSpec.Pipe

      val secureR = SecureReader(r, priv, pub)
      val secureW = SecureWriter(w, priv, pub)

      secureW.write(message)
      secureW.close()

      val read = IOUtils.toString(secureR)
      read must_== message
    }

    "ReadWriterPing counter example - Empty message" in {
      val keyPair = new KeyPair()
      val priv = keyPair.getPrivateKey.toBytes
      val pub  = keyPair.getPublicKey.toBytes

      val (r, w) = NaClSpec.Pipe

      val secureR = SecureReader(r, priv, pub)
      val secureW = SecureWriter(w, priv, pub)

      secureW.write("")
      secureW.close()

      val read = IOUtils.toString(secureR)
      read must_== ""
    }
  }
}
