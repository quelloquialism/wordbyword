package com.atsymboldot.wordbyword.source;

import com.atsymboldot.wordbyword.WordByWord;

/**
 * Simple interface representing a source of text that can be used as source
 * material for reading in {@link WordByWord}.
 */
public interface TextSource {
  public boolean hasNextWord();
  public String getNextWord();
}
