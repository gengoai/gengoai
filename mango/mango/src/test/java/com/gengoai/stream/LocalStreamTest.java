package com.gengoai.stream;

import org.junit.Before;

/**
 * @author David B. Bracewell
 */
public class LocalStreamTest extends BaseMStreamTest {

  @Before
  public void setUp() throws Exception {
    sc = StreamingContext.local();
  }

}// END OF JavaMStreamTest
