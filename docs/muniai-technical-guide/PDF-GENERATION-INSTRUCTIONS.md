# PDF generation instructions

A PDF was not generated automatically in this environment.

If you have Pandoc or Chromium available locally, run one of the following commands from the repository root:

```powershell
pandoc .\docs\muniai-technical-guide\MuniAI-Technical-Guide.md -o .\docs\muniai-technical-guide\MuniAI-Technical-Guide.pdf
```

or

```powershell
npx playwright install chromium
npx playwright pdf .\docs\muniai-technical-guide\MuniAI-Technical-Guide.html .\docs\muniai-technical-guide\MuniAI-Technical-Guide.pdf
```
