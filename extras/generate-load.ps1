# Copyright (C) 2023 - present, Juergen Zimmermann, Hochschule Karlsruhe
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

# https://docs.microsoft.com/en-us/powershell/scripting/developer/cmdlet/approved-verbs-for-windows-powershell-commands?view=powershell-7

# Aufruf:   .\generate-load.ps1 [ingress]

# ggf. vorher:  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
# oder:         Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser

# "Param" muss in der 1. Zeile sein
Param (
  [string]$ingress = ''
)

Set-StrictMode -Version Latest

$versionMinimum = [Version]'7.6.0'
$versionCurrent = $PSVersionTable.PSVersion
if ($versionMinimum -gt $versionCurrent) {
  throw "PowerShell $versionMinimum statt $versionCurrent erforderlich"
}

# Titel setzen
$host.ui.RawUI.WindowTitle = 'generate-load'

$schema = 'https'
$port = 8443

function Get-Access-Token {
  $body = @{
    username='admin'
    password='p'
  }

  $parameters = @{
    Method = 'Post'
    Uri = "${schema}`://localhost`:${port}/auth/token"
    Headers = @{
      'Content-Type' = 'application/json'
    }
    SkipHeaderValidation = $true
    HttpVersion = '2.0'
    SslProtocol = 'Tls13'
    SkipCertificateCheck = $true
    Body = ConvertTo-JSON($body)
  }

  # PSObject als Rueckgabetyp
  $token = Invoke-RestMethod @parameters
  return $token.access_token
}

function Get-Id {
  param([Int64] $index)

  $idPrefix = '00000000-0000-0000-0000-0000000000'

  switch ($index) {
    { $_ % 2 -eq 0 } {
      $id = "${idPrefix}20"
    }
    { $_ % 3 -eq 0 } {
      $id = "${idPrefix}30"
    }
    { $_ % 5 -eq 0 } {
      return
    }
    { $_ % 7 -eq 0 } {
      $id = "${idPrefix}40"
    }
    { $_ % 11 -eq 0 } {
      $id = "${idPrefix}50"
    }
    default {
      $id = "${idPrefix}01"
    }
  }

  return $id
}

function Invoke-GetRequest {
  param(
    [String] $uri,
    [String] $token
  )

  $parameters = @{
    Uri = $uri
    Headers = @{
      Accept = 'application/json'
    }
    SkipHeaderValidation = $true
    HttpVersion = '2.0'
    SslProtocol = 'Tls13'
    SkipCertificateCheck = $true
    Authentication = 'OAuth'
  }
  $bearerToken = ConvertTo-SecureString $token -AsPlainText -Force

  Invoke-RestMethod @parameters -Token $bearerToken | Out-Null
}

function Invoke-PostRequest {
  param(
    [String] $uri,
    [Int64] $index
  )

  $body = @{
    nachname='Testpost'
    email="Testpost${index}@test.de"
    kategorie=1
    hasNewsletter=$true
    geburtsdatum='2024-01-31'
    homepage='https://www.test.de'
    geschlecht='W'
    familienstand='L'
    adresse=@{
      plz='12345'
      ort='Testortpost'
    }
    rechnungen=@(@{
      betrag=1
      waehrung='EUR'
    })
    interessen=@('R', 'L')
    username="testpost$index"
    password='p'
  }

  $parameters = @{
    Method = 'Post'
    Uri = $uri
    Headers = @{
      'Content-Type' = 'application/json'
    }
    SkipHeaderValidation = $true
    HttpVersion = '2.0'
    SslProtocol = 'Tls13'
    SkipCertificateCheck = $true
    Body = ConvertTo-JSON($body)
  }
  Invoke-RestMethod @parameters
}

$accessToken = Get-Access-Token
Write-Host "Token = $accessToken"
Write-Host ''

for ($index = 1; ;$index++) {
  if ($index % 5 -ne 0) {
    $id = Get-Id $index
    Write-Host $id
    $uri = "${schema}`://localhost`:${port}/api/$id"
    Invoke-GetRequest $uri $accessToken
  } else {
    Write-Host -NoNewline "POST $index"
    Invoke-PostRequest "${schema}`://localhost`:${port}/api" $index
  }

  Start-Sleep -Seconds 0.5
}
