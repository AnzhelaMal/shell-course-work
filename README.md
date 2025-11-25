# Prerequisites

- Java
- Docker compose

# Build steps

1. Install choco:
   (Admin powershell)
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```
2. Install wix:
```powershell
choco install wixtoolset
```
3. Execute build script:
```powershell
./gradlew :launcher:buildExe
```