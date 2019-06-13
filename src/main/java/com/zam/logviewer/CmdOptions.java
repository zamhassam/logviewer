package com.zam.logviewer;

import org.apache.commons.cli.*;

import java.nio.file.Files;
import java.nio.file.Paths;

final class CmdOptions
{
    private final String[] fixXmls;
    private final String logFile;
    private final boolean nonInteractive;

    static CmdOptions parseCommandLineArgs(final String[] args)
    {
        final String fixXmlOptStr = "fix-xml";
        final String logFileOptStr = "log-file";
        final String nonInteractiveOptStr = "no-interactive";

        final Options options = new Options();
        final Option fixXmlsOpt = Option
                .builder()
                .argName("file")
                .desc("Provide a custom FIX XML file. Default behaviour is to infer the correct FIX XML.")
                .longOpt(fixXmlOptStr)
                .hasArgs()
                .required(false)
                .build();

        final Option logFileOpt = Option
                .builder()
                .argName("file")
                .desc("The log file to view. Default behaviour is to read from stdin.")
                .longOpt(logFileOptStr)
                .numberOfArgs(1)
                .required(false)
                .build();

        final Option interactiveOpt = new Option("n", nonInteractiveOptStr, false, "Read from the stdin and print to the terminal.");

        options.addOption(fixXmlsOpt);
        options.addOption(logFileOpt);
        options.addOption(interactiveOpt);

        final DefaultParser parser = new DefaultParser();
        final CommandLine parsed;
        try
        {
            parsed = parser.parse(options, args);
        }
        catch (final Exception e)
        {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("logviewer", options);
            throw new IllegalArgumentException(e);
        }

        final String[] fixXmls;
        final String logFile;
        if (parsed.hasOption(fixXmlOptStr))
        {
            fixXmls = parsed.getOptionValues(fixXmlOptStr);
            for (final String fixXml : fixXmls)
            {
                if (!Files.exists(Paths.get(fixXml)))
                {
                    throw new IllegalArgumentException("FIX XML does not exist: " + fixXml);
                }
            }
        }
        else
        {
            fixXmls = null;
        }

        final boolean nonInteractive = parsed.hasOption(nonInteractiveOptStr);

        if (parsed.hasOption(logFileOptStr))
        {
            logFile = parsed.getOptionValue(logFileOptStr);
            if (!Files.exists(Paths.get(logFile)))
            {
                throw new IllegalArgumentException("FIX XML does not exist: " + logFile);
            }
        }
        else
        {
            logFile = null;
        }

        return new CmdOptions(fixXmls, logFile, nonInteractive);
    }

    String[] getFixXmls()
    {
        return fixXmls;
    }

    String getLogFile()
    {
        return logFile;
    }

    public boolean isNonInteractive()
    {
        return nonInteractive;
    }

    private CmdOptions(final String[] fixXmls, final String logFile, final boolean nonInteractive)
    {
        this.fixXmls = fixXmls;
        this.logFile = logFile;
        this.nonInteractive = nonInteractive;
    }
}
