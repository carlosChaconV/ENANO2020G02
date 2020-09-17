@echo off
set NANO_JAR=lib\nanohttpd-2.3.1.jar
set NANO_WEB_SERVER=lib\nanohttpd-webserver-2.3.1.jar
set THE_SERVER=fi.iki.elonen.SimpleWebServer
set PORT=%1
set WEB_DIR=web
if "%PORT%"=="" set PORT=9000
echo Using port=%PORT% (%NANO_JAR%;%NANO_WEB_SERVER%)
REM [-h hostname] [-p port] [-d root-dir] [--licence])
java -cp %NANO_JAR%;%NANO_WEB_SERVER% %THE_SERVER% -p %PORT% -d %WEB_DIR%