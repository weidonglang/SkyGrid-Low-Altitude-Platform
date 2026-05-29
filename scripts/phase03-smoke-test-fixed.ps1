$ErrorActionPreference = 'Stop'

function Show-Json($obj) {
    $obj | ConvertTo-Json -Depth 12 | Write-Host
}

try {
    Write-Host '[Phase03] Checking gateway health...'
    $health = Invoke-RestMethod 'http://127.0.0.1:8080/actuator/health'
    Show-Json $health

    Write-Host '[Phase03] Requesting ADMIN dev token...'
    $tokenResp = Invoke-RestMethod -Method Post 'http://127.0.0.1:8080/api/auth/dev-token?username=demo&role=ADMIN'
    Show-Json $tokenResp

    $token = $tokenResp.data.accessToken
    if ([string]::IsNullOrWhiteSpace($token)) {
        throw 'accessToken is empty. Check /api/auth/dev-token response.'
    }
    $headers = @{ Authorization = ('Bearer ' + $token) }
    Write-Host '[Phase03] Token acquired.'

    $date = (Get-Date).AddDays(1).ToString('yyyy-MM-dd')
    $bodyObj = @{
        taskName        = 'Phase03 demo inspection'
        orgId           = 1
        applicantUserId = 1
        applicantName   = 'demo'
        routeTemplateId = 1
        levelId         = 1
        bookingDate     = $date
        timeSlotIds     = @(1, 2)
        applyReason     = 'Phase03 smoke test'
        description     = 'submit -> approve -> occupancy -> cancel'
    }
    $json = $bodyObj | ConvertTo-Json -Depth 8

    Write-Host '[Phase03] Submitting booking...'
    $created = Invoke-RestMethod -Method Post `
        -Headers $headers `
        -ContentType 'application/json; charset=utf-8' `
        -Body $json `
        'http://127.0.0.1:8080/api/bookings'
    Show-Json $created

    $id = $created.data.id
    if (-not $id) {
        throw 'created booking id is empty. Check POST /api/bookings response.'
    }
    Write-Host ('[Phase03] Created booking id=' + $id)

    Write-Host '[Phase03] Getting booking detail...'
    $detail = Invoke-RestMethod -Headers $headers ('http://127.0.0.1:8080/api/bookings/' + $id)
    Show-Json $detail

    Write-Host '[Phase03] Approving booking...'
    $approveJson = @{
        operatorUserId = 1
        operatorName   = 'admin'
        comment        = 'approved by phase03 smoke test'
    } | ConvertTo-Json -Depth 5
    $approved = Invoke-RestMethod -Method Post `
        -Headers $headers `
        -ContentType 'application/json; charset=utf-8' `
        -Body $approveJson `
        ('http://127.0.0.1:8080/api/bookings/' + $id + '/approve')
    Show-Json $approved

    Write-Host '[Phase03] Checking occupancies...'
    $occ = Invoke-RestMethod -Headers $headers ('http://127.0.0.1:8080/api/bookings/' + $id + '/occupancies')
    Show-Json $occ
    if (-not $occ.data -or $occ.data.Count -lt 1) {
        throw 'occupancy list is empty after approval.'
    }

    Write-Host '[Phase03] Cancelling booking and releasing occupancies...'
    $cancelJson = @{
        operatorUserId = 1
        operatorName   = 'admin'
        cancelReason   = 'phase03 smoke test finished'
    } | ConvertTo-Json -Depth 5
    $cancelled = Invoke-RestMethod -Method Post `
        -Headers $headers `
        -ContentType 'application/json; charset=utf-8' `
        -Body $cancelJson `
        ('http://127.0.0.1:8080/api/bookings/' + $id + '/cancel')
    Show-Json $cancelled

    Write-Host '[Phase03] Checking flows...'
    $flows = Invoke-RestMethod -Headers $headers ('http://127.0.0.1:8080/api/bookings/' + $id + '/flows')
    Show-Json $flows

    Write-Host '[Phase03] OK'
    exit 0
}
catch {
    Write-Host ''
    Write-Host '[Phase03] ERROR:' -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ScriptStackTrace) {
        Write-Host ''
        Write-Host '[Phase03] Script stack trace:' -ForegroundColor Yellow
        Write-Host $_.ScriptStackTrace
    }
    exit 1
}
