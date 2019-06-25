package com.zam.logviewer;

import java.util.List;


import picocli.CommandLine;

final class CmdOptions
{
    @CommandLine.Option(names = {"-f", "--fix-xml"}, description = "Provide a custom FIX XML file. Default behaviour is to infer the correct FIX XML.", split = ",")
    private String[] fixXmls;
    @CommandLine.Option(names = {"-l", "--log-file"}, description = "The log file to view. Default behaviour is to read from stdin.")
    private String logFile;
    @CommandLine.Option(names = "-n", description = "Read from the stdin and print to the terminal.")
    private boolean nonInteractive;
    private String fixLines;

    private CmdOptions()
    {
    }

    String[] getFixXmls()
    {
        return fixXmls;
    }

    String getLogFile()
    {
        return logFile;
    }

    public String getFixLines()
    {
        return fixLines;
    }

    public boolean isNonInteractive()
    {
        return nonInteractive;
    }

    static CmdOptions parseCommandLineArgs(final String[] args)
    {
        final CmdOptions cmdOptions = new CmdOptions();
        final CommandLine commandLine = new CommandLine(cmdOptions).setUnmatchedArgumentsAllowed(true);
        commandLine.parse(args);
        final List<String> fixStrings = commandLine.getParseResult().unmatched();
        cmdOptions.fixLines = String.join("\n", fixStrings);
        return cmdOptions;
    }

}
