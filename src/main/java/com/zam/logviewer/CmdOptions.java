package com.zam.logviewer;

import org.apache.commons.cli.*;

import java.nio.file.Files;
import java.nio.file.Paths;

final class CmdOptions
{
    private final String[] fixXmls;
    private final String logFile;

    static CmdOptions parseCommandLineArgs(final String[] args)
    {
        final String fixXmlOptStr = "fix-xml";
        final String logFileOptStr = "log-file";

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

        options.addOption(fixXmlsOpt);
        options.addOption(logFileOpt);

        final DefaultParser parser = new DefaultParser();
        final CommandLine parsed;
        try
        {
            parsed = parser.parse(options, args);
        }
        catch (final Exception e)
        {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "logviewer", options );
            throw new IllegalArgumentException(e);
        }

        final String[] fixXmls;
        final String logFile;
        if (parsed.hasOption(fixXmlOptStr))
        {
            fixXmls = parsed.getOptionValues(fixXmlOptStr);
            for (final String fixXml : fixXmls)
            {
                if (! Files.exists(Paths.get(fixXml)))
                {
                    throw new IllegalArgumentException("FIX XML does not exist: " + fixXml);
                }
            }
        }
        else
        {
            fixXmls = null;
        }

        if (parsed.hasOption(logFileOptStr))
        {
            logFile = parsed.getOptionValue(logFileOptStr);
            if (! Files.exists(Paths.get(logFile)))
            {
                throw new IllegalArgumentException("FIX XML does not exist: " + logFile);
            }
        }
        else
        {
            logFile = null;
        }

        return new CmdOptions(fixXmls, logFile);
    }

    String[] getFixXmls()
    {
        return fixXmls;
    }

    String getLogFile()
    {
        return logFile;
    }

    private CmdOptions(final String[] fixXmls, final String logFile)
    {
        this.fixXmls = fixXmls;
        this.logFile = logFile;
    }
}
