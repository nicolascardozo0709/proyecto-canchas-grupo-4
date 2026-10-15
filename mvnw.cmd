@echo off
pushd "%~dp0backend"
call mvnw.cmd %*
popd
