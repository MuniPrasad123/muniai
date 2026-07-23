# Native PostgreSQL setup on Windows

Use a native PostgreSQL installation, not Docker. Install a supported release using the official Windows installer, retain port `5432`, choose a strong `postgres` administrator password, and include command-line tools. Never place the password in Git.

## Loopback restriction

Set these values in `postgresql.conf`:

```conf
listen_addresses = 'localhost'
password_encryption = 'scram-sha-256'
```

Allow this application through loopback in `pg_hba.conf`:

```conf
host  muniai  muniai  127.0.0.1/32  scram-sha-256
host  muniai  muniai  ::1/128       scram-sha-256
```

Restart the PostgreSQL Windows service, then check `pg_isready -h 127.0.0.1 -p 5432`.

## Create role and database

Open SQL Shell (`psql`) as `postgres`. Replace the example password with a unique local secret.

```sql
CREATE ROLE muniai LOGIN PASSWORD 'choose-a-unique-local-password';
CREATE DATABASE muniai OWNER muniai;
REVOKE ALL ON DATABASE muniai FROM PUBLIC;
GRANT CONNECT, TEMPORARY ON DATABASE muniai TO muniai;
```

Flyway creates and validates tables when the backend starts.

## Environment and startup

```powershell
$env:MUNIAI_DB_URL = 'jdbc:postgresql://127.0.0.1:5432/muniai'
$env:MUNIAI_DB_USERNAME = 'muniai'
$env:MUNIAI_DB_PASSWORD = '<your-local-password>'
$env:OLLAMA_BASE_URL = 'http://127.0.0.1:11434'
$env:MUNIAI_CHAT_MODEL = 'llama3.2:3b'

cd backend/muniai-api
mvn spring-boot:run
```

In another terminal:

```powershell
cd frontend/muniai-web
npm install
npm run dev
```

Open `http://127.0.0.1:5173`. Send a message, refresh, restart the backend, and confirm the conversation remains.

## Tests

```powershell
cd backend/muniai-api
mvn test

cd ../../frontend/muniai-web
npm test -- --run
npm run build
```

Backups are manual in Phase 4. If needed, run `pg_dump -h 127.0.0.1 -U muniai -Fc muniai -f <protected-path>`. Backups contain sensitive prompts and responses and must be protected.
