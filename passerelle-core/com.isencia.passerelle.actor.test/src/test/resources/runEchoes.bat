@echo off
if "%OS%" == "Windows_NT" setlocal

echo this is a first output to stdout
echo and now for something completely different

echo some error msg>&2
exit 0

