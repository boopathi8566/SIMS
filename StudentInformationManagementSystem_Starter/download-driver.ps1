$url = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
$out = Join-Path $PSScriptRoot "lib\mysql-connector-j-8.4.0.jar"
New-Item -ItemType Directory -Force -Path (Split-Path $out) | Out-Null
Invoke-WebRequest -Uri $url -OutFile $out
Write-Host "MySQL Connector/J downloaded to $out"
