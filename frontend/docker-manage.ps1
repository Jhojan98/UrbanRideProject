#!/usr/bin/env pwsh

# Script de ayuda para gestionar contenedores Docker de Frontend
# Uso: .\docker-manage.ps1 [comando]

param(
    [Parameter(Position = 0)]
    [string]$Command = ""
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

# Obtener la ruta del script
$ScriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $ScriptPath

# Colores
$Colors = @{
    Info    = "Cyan"
    Success = "Green"
    Warning = "Yellow"
    Error   = "Red"
}

# Funciones auxiliares
function Write-Info {
    param([string]$Message)
    Write-Host "ℹ $Message" -ForegroundColor $Colors.Info
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor $Colors.Success
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor $Colors.Warning
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor $Colors.Error
}

# Verificar que Docker está instalado
function Test-Docker {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    $dockerCompose = Get-Command docker-compose -ErrorAction SilentlyContinue

    if (-not $docker) {
        Write-Error-Custom "Docker no está instalado"
        exit 1
    }

    if (-not $dockerCompose) {
        Write-Error-Custom "Docker Compose no está instalado"
        exit 1
    }

    Write-Success "Docker detectado"
}

# Crear red si no existe
function Ensure-Network {
    $network = docker network ls --filter "name=urbanride-network" -q

    if (-not $network) {
        Write-Info "Creando red urbanride-network..."
        docker network create urbanride-network | Out-Null
        Write-Success "Red creada"
    }
    else {
        Write-Success "Red urbanride-network ya existe"
    }
}

# Mostrar menú
function Show-Menu {
    Write-Host ""
    Write-Host "=== Frontend Docker Manager ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Construir e iniciar (producción)"
    Write-Host "2. Construir e iniciar (desarrollo)"
    Write-Host "3. Detener contenedores"
    Write-Host "4. Detener y eliminar"
    Write-Host "5. Ver logs (admin)"
    Write-Host "6. Ver logs (client)"
    Write-Host "7. Reconstruir sin caché"
    Write-Host "8. Estado de contenedores"
    Write-Host "9. Salir"
    Write-Host ""
}

# Funciones principales
function Start-Production {
    Write-Info "Iniciando en modo producción..."
    Ensure-Network
    docker-compose up -d --build
    Write-Success "Contenedores iniciados"
    Write-Info "Admin: http://localhost:8001"
    Write-Info "Client: http://localhost:8002"
}

function Start-Development {
    Write-Info "Iniciando en modo desarrollo..."
    Ensure-Network
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
    Write-Success "Contenedores iniciados en modo desarrollo"
    Write-Info "Admin: http://localhost:8001"
    Write-Info "Client: http://localhost:8002"
}

function Stop-Containers {
    Write-Info "Deteniendo contenedores..."
    docker-compose stop
    Write-Success "Contenedores detenidos"
}

function Remove-Containers {
    Write-Info "Deteniendo y eliminando contenedores..."
    docker-compose down
    Write-Success "Contenedores eliminados"
}

function View-AdminLogs {
    Write-Info "Mostrando logs de admin-frontend..."
    docker logs -f admin-frontend
}

function View-ClientLogs {
    Write-Info "Mostrando logs de client-frontend..."
    docker logs -f client-frontend
}

function Rebuild-NoCache {
    Write-Info "Reconstruyendo sin caché..."
    Ensure-Network
    docker-compose up -d --build --no-cache
    Write-Success "Reconstrucción completada"
}

function Show-Status {
    Write-Info "Estado de contenedores:"
    docker-compose ps
}

# Main
function Main {
    Test-Docker

    if ([string]::IsNullOrWhiteSpace($Command)) {
        # Modo interactivo
        while ($true) {
            Show-Menu
            $choice = Read-Host "Selecciona una opción (1-9)"
            Write-Host ""

            switch ($choice) {
                "1" { Start-Production }
                "2" { Start-Development }
                "3" { Stop-Containers }
                "4" { Remove-Containers }
                "5" { View-AdminLogs }
                "6" { View-ClientLogs }
                "7" { Rebuild-NoCache }
                "8" { Show-Status }
                "9" {
                    Write-Info "Saliendo..."
                    exit 0
                }
                default { Write-Error-Custom "Opción inválida" }
            }
        }
    }
    else {
        # Modo comando directo
        switch ($Command) {
            { $_ -in "prod", "production" } { Start-Production }
            { $_ -in "dev", "development" } { Start-Development }
            "stop" { Stop-Containers }
            { $_ -in "remove", "down" } { Remove-Containers }
            "logs:admin" { View-AdminLogs }
            "logs:client" { View-ClientLogs }
            "rebuild" { Rebuild-NoCache }
            { $_ -in "status", "ps" } { Show-Status }
            default {
                Write-Error-Custom "Comando desconocido: $Command"
                Write-Host "Comandos disponibles: prod, dev, stop, remove, logs:admin, logs:client, rebuild, status"
                exit 1
            }
        }
    }
}

try {
    Main
}
finally {
    Pop-Location
}
