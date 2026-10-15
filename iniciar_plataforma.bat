@echo off
title Plataforma de Reserva de Canchas - Pontificia Universidad Javeriana
color 0A
cls
echo ==============================================================================
echo   PONTIFICIA UNIVERSIDAD JAVERIANA - PERIODO 2026-3
echo   PLATAFORMA DE RESERVA DE ESCENARIOS DEPORTIVOS (GRUPO 4)
echo ==============================================================================
echo.
echo [1/2] Iniciando servidor Spring Boot y base de datos permanente en disco...
echo [2/2] Activando automaticamente el enlace publico en Internet...
echo.
echo Apenas inicie, aparecera tu enlace publico para abrir desde cualquier celular.
echo.
cd /d "%~dp0backend"
call .\mvnw.cmd spring-boot:run
pause
