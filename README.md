# node-mcu-interpreter
The project which pretends LUA interpreter. For example, to use in Eclipse plugin Lua Development Tools

##THIS TOOL IS IN REALLY REALLY REALLY REALLY EARLY STAGE
###You use interpreter at your own risk!
##Tested only on Windows operating system.


###Help:
```
java -jar interpreter.jar [OPTIONS] <MAIN_FILE>
java -jar interpreter.jar -h|--help

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
```

###Eclipse
Window->Preferences->Lua->Interpreters->Add->InterpreterType:GenericLua;Interpreter executable:EXEC_INTERPRETER<br />