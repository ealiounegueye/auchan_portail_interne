@echo off
cd /d "%~dp0frontend"
if not exist node_modules (
  echo Installation des dependances Angular...
  call npm install
)
call npm start
