package org.nanomvc.http;

public class LocalFile
{
  private String absolutePath;
  private String relativePath;
  private String fileName;

  public LocalFile()
  {
  }

  public LocalFile(String absolutePath, String relativePath, String fileName)
  {
    this.absolutePath = absolutePath;
    this.relativePath = relativePath;
    this.fileName = fileName;
  }

  public String getAbsolutePath() {
    return this.absolutePath;
  }

  public void setAbsolutePath(String absolutePath) {
    this.absolutePath = absolutePath;
  }

  public String getRelativePath() {
    return this.relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}