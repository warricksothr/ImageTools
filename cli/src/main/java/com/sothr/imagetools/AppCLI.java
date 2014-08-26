package com.sothr.imagetools;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.sothr.imagetools.engine.CLIEngineListener;
import com.sothr.imagetools.engine.ConcurrentEngine;
import com.sothr.imagetools.engine.Engine;
import com.sothr.imagetools.image.SimilarImages;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.immutable.List;

/**
 * CLI interface for Image Tools
 */
class AppCLI {

  private static Logger logger;

  private static final String HEADER = "Process images and search for duplicates and similar images heuristically";
  private static final String FOOTER = "Please report issues to...";

  public static void main(String[] args) {
    try {
      Options options = getOptions();
      CommandLineParser parser = new BasicParser();
      CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption('h') || cmd.getOptions().length < 1 || cmd.getArgs().length > 0) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Image-Tools CLI", HEADER, options, FOOTER, true);
      } else {
        AppConfig.configureApp();
        logger = LoggerFactory.getLogger(AppCLI.class);
        logger.info("Started Image Tools CLI");
        process(cmd);
        AppConfig.shutdown();
      }
      System.exit(0);
    }  catch (Exception ex) {
      logger.error("Unhandled exception in AppCLI",ex);
    }
  }

  private static Options getOptions() {
    //scan a list of directories
    Options options = new Options();

    //show help
    Option helpOption = OptionBuilder.create('h');
    helpOption.setLongOpt("help");
    helpOption.setDescription("Display this help dialog");
    options.addOption(helpOption);

    //scan directories
    Option scanOption = OptionBuilder.create('s');
    scanOption.setLongOpt("scan");
    scanOption.setDescription("Scan directories for a list of similar images");
    scanOption.setArgs(1);
    scanOption.setArgName("DIRECTORY");
    options.addOption(scanOption);

    //scan directories in a recursive manner
    Option recursiveOption = OptionBuilder.create('r');
    recursiveOption.setLongOpt("recursive");
    recursiveOption.setDescription("Scan directories recursively");
    options.addOption(recursiveOption);

    //depth limit
    Option depthOption = OptionBuilder.create('d');
    depthOption.setLongOpt("depth");
    depthOption.setDescription("Limit the maximum depth of the recursive search");
    depthOption.setArgs(1);
    depthOption.setArgName("INTEGER");
    options.addOption(depthOption);
    return options;
  }

  private static void process(CommandLine cmd) {
    //scan a comma separated list of paths to search for image similarities
    try {
      Engine engine = new ConcurrentEngine();

      //create the listeners that will be passed onto the actors
      ActorSystem system = AppConfig.getAppActorSystem();
      Props cliListenerProps = Props.create(CLIEngineListener.class);
      ActorRef cliListener = system.actorOf(cliListenerProps);

      //set the listeners
      engine.setProcessedListener(cliListener);
      engine.setSimilarityListener(cliListener);


      if (cmd.hasOption('s')) {
        Boolean recursive = false;
        Integer recursiveDepth = 500;
        if (cmd.hasOption('r')) {
            recursive = true;
        }
        if (cmd.hasOption('d')) {
          recursiveDepth = Integer.parseInt(cmd.getOptionValue('d'));
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
    } catch (Exception ex) {
      throw new IllegalArgumentException("One or more arguments could not be parsed correctly", ex);
    }
  }
}
