LogViewer
===========================

There are two ways of being able to execute via the `logviewer.sh` script
```
./logviewer.sh --log-file fix_files.log --fix-xml FIXT.xml,FIX50SP2.xml
```

```
grep 20190614 fix_files.log | ./logviewer.sh --fix-xml FIXT.xml,FIX50SP2.xml
```

Recommend aliasing the script for easier use
```
alias logviewer=~/repos/logviewer/logviewer.sh
```