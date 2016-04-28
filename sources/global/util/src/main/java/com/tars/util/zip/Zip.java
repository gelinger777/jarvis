package com.tars.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.io.IOUtils.toByteArray;
import static util.global.ExceptionHandlingKt.wtf;
import static util.global.ValidationKt.condition;
import static util.global.ValidationKt.notNullOrEmpty;
/**
 * Immutable zip representation.
 */
public class Zip {

  private final byte[] bytes;

  private Zip(byte[] bytes) {
    if (bytes == null) {
      throw new IllegalStateException();
    }
    this.bytes = bytes;
  }

  /**
   * @return byte[] representation of the zip.
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * Iterate trough all files and execute the provided callback.
   */
  public void iterate(ZipEntryCallback callback) {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
      ZipEntry nextEntry = zis.getNextEntry();

      while (nextEntry != null) {
        callback.process(
            toByteArray(zis), // we don't want the callback to access the stream itself
            nextEntry
        );
        nextEntry = zis.getNextEntry();
      }
      zis.closeEntry();

    } catch (IOException cause) {
      throw new IllegalStateException(cause);
    }
  }

  /**
   * Creates builder with containing already existing content.
   */
  public Builder toBuilder() {
    Builder builder = builder();
    this.iterate((byte[] content, ZipEntry entry) -> builder.addEntry(entry.getName(), content));
    return builder;
  }

  // types

  public interface ZipEntryCallback {

    /**
     * Invoked for each entry in a ZIP file.
     *
     * @param in       contents of the ZIP entry.
     * @param zipEntry ZIP entry.
     */
    void process(byte[] in, ZipEntry zipEntry);

  }

  // static factory methods

  public static Zip from(Path path) {
    return from(path.toFile());
  }

  public static Zip from(File file) {
    return new Builder().addEntry(file).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  // builder

  /**
   * Zip builder utility for composing zip archives on the fly. Not thread safe.
   */
  public static class Builder {

    private final Map<String, byte[]> entries = new HashMap<>();

    private Builder() {
    }

    public Builder addEntry(String name, byte[] bytes) {
      condition(notNullOrEmpty(name) && !entries.containsKey(name));
      entries.put(name, bytes);
      return this;
    }

    public Builder addEntry(Path path) {
      return addEntry(path.toFile());
    }

    public Builder addEntry(File file) {
      try {
        String rootPath = file.getPath();

        if (file.isDirectory()) {
          // recursively add all files
          traverse(rootPath, file);
        } else if (file.isFile()) {
          // zip with only one file in it
          addEntry(
              file.getName(),
              toByteArray(new FileInputStream(file))
          );
        } else {
          wtf();
        }

        return this;
      } catch (Exception cause) {
        throw new IllegalStateException(cause);
      }
    }

    public Zip build() {
      try (
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ZipOutputStream zos = new ZipOutputStream(baos)
      ) {
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
          ZipEntry e = new ZipEntry(entry.getKey());
          zos.putNextEntry(e);
          byte[] value = entry.getValue();

          if (value != null && value.length != 0) {
            zos.write(value);
          }

          zos.closeEntry();
        }

        zos.finish();
        return new Zip(baos.toByteArray());
      } catch (IOException cause) {
        throw new IllegalStateException(cause);
      }
    }

    // stuff


    private void traverse(String rootPath, File file) throws IOException {
      // if directory recursively add all leafs of the directory tree
      if (file.isDirectory()) {
        File[] files = file.listFiles();

        // recursive call on each leaf
        if (files != null && files.length > 0) {
          for (File child : files) {
            traverse(rootPath, child);
          }
        }
        // empty directory
        else {
          addEntry(
              relativeName(rootPath, file.getPath()) + File.separator,
              null
          );
        }
      }

      // if file add zip entry
      else if (file.isFile()) {
        addEntry(
            relativeName(rootPath, file.getPath()),
            toByteArray(new FileInputStream(file))
        );
      }

      // unexpected case (failing fast)
      else {
        wtf();
      }
    }

    private String relativeName(String rootPath, String filePath) {
      condition(!filePath.startsWith(rootPath));
      return filePath.substring(rootPath.length() + 1, filePath.length());
    }
  }
}
