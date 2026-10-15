$ErrorActionPreference = 'Stop'

$gradleVersion = '9.6.0'
$toolCache = Join-Path $env:LOCALAPPDATA 'FlappyLulaBuildTools'
$gradleHome = Join-Path $toolCache "gradle-$gradleVersion"
$gradleExecutable = Join-Path $gradleHome 'bin\gradle.bat'
$archive = Join-Path $toolCache "gradle-$gradleVersion-bin.zip"
$projectGradleHome = Join-Path $PSScriptRoot '.gradle-user-home'
$projectAndroidHome = Join-Path $projectGradleHome '.android'

$jdkCandidates = @(
    (Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'),
    $env:JAVA_HOME,
    (Join-Path $env:ProgramFiles 'Java\jdk-17')
) | Where-Object { $_ }
$javaHome = $null
foreach ($candidate in $jdkCandidates) {
    $releaseFile = Join-Path $candidate 'release'
    if (Test-Path -LiteralPath $releaseFile) {
        $releaseInfo = Get-Content -LiteralPath $releaseFile -Raw
        if ($releaseInfo -match 'JAVA_VERSION="(?<major>\d+)' -and [int]$Matches.major -ge 17) {
            $javaHome = $candidate
            break
        }
    }
}
if (-not $javaHome) {
    throw 'É necessário um JDK 17 ou superior para compilar o app Android.'
}
$env:JAVA_HOME = $javaHome

if (-not (Test-Path -LiteralPath $gradleExecutable)) {
    New-Item -ItemType Directory -Force -Path $toolCache | Out-Null
    $url = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
    Write-Host "Baixando Gradle $gradleVersion..."
    Invoke-WebRequest -Uri $url -OutFile $archive
    Expand-Archive -LiteralPath $archive -DestinationPath $toolCache -Force
    Remove-Item -LiteralPath $archive -Force
}

$sdkRoot = $env:ANDROID_SDK_ROOT
if (-not $sdkRoot) {
    $sdkRoot = $env:ANDROID_HOME
}
if (-not $sdkRoot) {
    $sdkRoot = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}
if (-not (Test-Path -LiteralPath (Join-Path $sdkRoot 'platforms\android-37.0'))) {
    throw "Android SDK Platform 37 não encontrado em '$sdkRoot'. Instale-o pelo SDK Manager do Android Studio."
}

$env:ANDROID_SDK_ROOT = $sdkRoot
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_USER_HOME = $projectAndroidHome
$env:ANDROID_SDK_HOME = $projectGradleHome
New-Item -ItemType Directory -Force -Path $projectGradleHome, $projectAndroidHome | Out-Null
Push-Location $PSScriptRoot
try {
    & $gradleExecutable --gradle-user-home $projectGradleHome --no-daemon `
        "-Dorg.gradle.java.home=$javaHome" "-Duser.home=$projectGradleHome" assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw "A compilação do APK falhou (código $LASTEXITCODE)."
    }
} finally {
    Pop-Location
}

$apk = Join-Path $PSScriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
Write-Host "APK criado: $apk"
