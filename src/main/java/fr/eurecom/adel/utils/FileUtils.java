package fr.eurecom.adel.utils;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Julien Plu
 */
public class FileUtils {
  public static List<Path> getFilesWithPattern(final Path folder, final String pattern) throws
      IOException {
    final List<Path> files = new ArrayList<>();
    
    Files.walk(folder)
        .filter(p -> p.toString().endsWith(pattern))
        .distinct()
        .forEach(files::add);
    
    return files;
  }
  
  public static void uncompressArchive(final String fileIn, final String fileOut,
                                       final String algoName) throws CompressorException,
      IOException {
    final InputStream is = Files.newInputStream(Paths.get(fileIn));
    final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream(
        algoName, is);
    
    IOUtils.copy(in, Files.newOutputStream(Paths.get(fileOut)));
    
    in.close();
  }
  
  public static void compressArchive(final String fileIn, final String fileOut,
                                     final String algoName) throws CompressorException,
      IOException {
    final OutputStream out = Files.newOutputStream(Paths.get(fileOut));
    final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream(
        algoName, out);
  
    IOUtils.copy(Files.newInputStream(Paths.get(fileIn)), cos);
    
    cos.close();
  }
  
  public static void deleteFolder(final Path dir) throws IOException {
    Files.list (dir).forEach (file -> {
      try {
        Files.deleteIfExists (file);
      } catch (IOException ex) {
        throw new WebApplicationException("Failed to delete the content from " + dir,
            ex, Response.Status.PRECONDITION_FAILED);
      }
    });
  
    Files.delete (dir);
  }
}
