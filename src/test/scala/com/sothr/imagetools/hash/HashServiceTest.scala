package com.sothr.imagetools.hash

import com.sothr.imagetools.BaseTest
import javax.imageio.ImageIO
import java.io.File

/**
 * Created by dev on 1/23/14.
 */
class HashServiceTest extends BaseTest {

  test("Calculate DHash") {
    val sample = new File("./target/sample/sample_01_large.jpg")
    val image = ImageIO.read(sample)
    val hash = HashService.getDhash(image)
    assert(hash == 0L)
  }

}
