package com.gengoai.stream;

import org.junit.Before;

/**
 * @author David B. Bracewell
 */
public class LocalPairStreamTest extends BaseMPairStreamTest {

  @Before
  public void setUp() throws Exception {
    sc = StreamingContext.local();
  }

}// END OF JavaMPairStreamTest
