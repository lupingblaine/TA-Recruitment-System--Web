# TA&MO 本地启动（Windows）：自动设置 JAVA_HOME，优先使用用户目录下的 Maven 便携版
$ErrorActionPreference = "Stop"
$javaExe = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $javaExe) {
    Write-Error "未找到 java，请先安装 JDK 17+ 并加入 PATH。"
}
$env:JAVA_HOME = (Get-Item $javaExe).Directory.Parent.FullName

$mvnLocal = Join-Path $env:USERPROFILE ".local\apache-maven-3.9.14\bin\mvn.cmd"
$mvn = if (Test-Path $mvnLocal) { $mvnLocal } else { "mvn" }

Set-Location $PSScriptRoot
if ($args.Count -eq 0) {
    & $mvn compile exec:java
} else {
    & $mvn @args
}
