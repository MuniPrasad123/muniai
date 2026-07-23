# Document API

Base path: `/api/v1/documents`

| Method | Path | Result |
|---|---|---|
| `POST` | `/api/v1/documents` | Upload multipart part `file`; returns `201` metadata |
| `GET` | `/api/v1/documents` | Metadata newest first |
| `GET` | `/api/v1/documents/{id}` | One metadata record |
| `GET` | `/api/v1/documents/{id}/text` | Extracted text after completion |
| `DELETE` | `/api/v1/documents/{id}` | Delete original and metadata; returns `204` |

Metadata contains the ID, safe original name, type, size, status, optional extraction error, optional page count, file availability, and timestamps. It never contains a filesystem path or internal filename.

```powershell
$result = Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/documents -Form @{ file = Get-Item ./sample.txt }
Invoke-RestMethod "http://127.0.0.1:8080/api/v1/documents/$($result.id)/text"
Invoke-RestMethod -Method Delete "http://127.0.0.1:8080/api/v1/documents/$($result.id)"
```

Validation uses shared safe errors such as `EMPTY_DOCUMENT`, `DOCUMENT_TOO_LARGE`, `UNSUPPORTED_DOCUMENT_TYPE`, and `INVALID_DOCUMENT_SIGNATURE`. Failed extraction is represented in metadata. Unavailable text returns `409 DOCUMENT_TEXT_UNAVAILABLE`; an unknown ID returns `404 DOCUMENT_NOT_FOUND`.
