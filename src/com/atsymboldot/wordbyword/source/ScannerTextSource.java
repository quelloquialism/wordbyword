package com.atsymboldot.wordbyword.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/**
 * A {@link TextSource} that is backed by a {@link java.util.Scanner} for reading and tokenizing
 * an input stream from various underlying sources.
 */
public class ScannerTextSource implements TextSource {
  private Scanner source;
  
  public ScannerTextSource(InputStream stream) {
    this.source = new Scanner(stream);
  }
  
  public ScannerTextSource(File file) throws FileNotFoundException {
    this.source = new Scanner(file);
  }
  
  public ScannerTextSource(String rawText) {
    this.source = new Scanner(rawText);
  }
  
  public ScannerTextSource(URL url) throws IOException {
    this.source = new Scanner(url.openStream());
  }
  
  @Override
  public boolean hasNextWord() {
    return source.hasNext();
  }

  @Override
  public String getNextWord() {
    return source.next();
  }
}
