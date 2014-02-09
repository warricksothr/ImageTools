package com.sothr.imagetools;

import com.sothr.imagetools.image.SimilarImages;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.immutable.List;

/**
 * CLI interface for Image Tools
 */
class AppCLI {

  private static Logger logger;

  public static void main(String[] args) {
    AppConfig.configureApp();
    logger = LoggerFactory.getLogger(AppCLI.class);
    logger.info("Started Image Tools CLI");
    try {
      Options options = getOptions();
      CommandLineParser parser = new BasicParser();
      CommandLine cmd = parser.parse(options, args);
      process(cmd);
      AppConfig.shutdown();
      System.exit(0);
    }  catch (Exception ex) {
      logger.error("Unhandled exception in AppCLI",ex);
    }
  }

  private static Options getOptions() {
    //scan a list of directories
    Options options = new Options();
    options.addOption(new Option("s", true, "scan directories for a list of similar images"));
    //scan directories in a recursive manner
    options.addOption(new Option("r", false, "scan directories recursively"));
    return options;
  }

  private static void process(CommandLine cmd) {
    //scan a comma separated list of paths to search for image similarities
    Engine engine = new ConcurrentEngine();
    if (cmd.hasOption('s')) {
      Boolean recursive = false;
      Integer recursiveDepth = 500;
      if (cmd.hasOption('r')) {
          recursive = true;
      } 
      String scanList = cmd.getOptionValue('s');
      String[] paths = scanList.split(",");
      for (String path : paths) {
        List<SimilarImages> similarImages = engine.getSimilarImagesForDirectory(path, recursive, recursiveDepth);
        for (int index = 0; index < similarImages.length(); index++) {
          SimilarImages similar = similarImages.apply(index);
          System.out.println(similar.toString());
        }
      }
    }
  }

}
