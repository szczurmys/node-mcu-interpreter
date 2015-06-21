# node-mcu-interpreter
The project which pretends LUA interpreter. For example, to use in Eclipse plugin Lua Development Tools

##THIS TOOL IS IN REALLY REALLY REALLY REALLY EARLY STAGE
###You use interpreter at your own risk!
##Tested only on Windows operating system.


###Help:
```
node-mcu-interpreter-0.3-alpha_with_jssc_lib.exe [OPTIONS] <MAIN_FILE>
node-mcu-interpreter-0.3-alpha_with_jssc_lib.exe -h|--help

MAIN_FILE - file to run and copy file from parent directory.
OPTIONS:
  -h or --help              - show this message
  -e=file1,...,file         - excludes file, path can be relative for parent directory
  -l=END_COMMAND            - end of command in esp firmware, default \r\n
  -d=PARENT_DIRECTORY       - parent directory, default - parent directory for MAIN_FILE
  -o                        - send only main file
  -p=PORT                   - serial port
  -f                        - select first port
  -nr                       - not execute (dofile), only save
  -t=TIMEOUT                - timeout, default - 10000 [ms]
  -R                        - only remove files from device
  -i                        - ignore files in directories
  -nw                       - not wait for output
  -b=BAUD_RATE              - baud rate, default - 9600
  -c                        - compile *.lua files.
  -cr                       - compile and next remove *.lua files (include -c)
  -ic=file1,...,file        - ignore file to compile
```

###Eclipse
Window->Preferences->Lua->Interpreters->Add->InterpreterType:GenericLua;InterpreterExecutable:EXEC_INTERPRETER
