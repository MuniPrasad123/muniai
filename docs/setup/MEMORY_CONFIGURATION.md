# Memory configuration

| Environment variable | Default | Meaning |
|---|---:|---|
| `MEMORY_ENABLED` | `true` | Enables controlled memory APIs and selection |
| `MEMORY_MAX_ACTIVE` | `100` | Maximum active records |
| `MEMORY_MAX_PROPOSED` | `50` | Maximum pending proposals |
| `MEMORY_MAX_VALUE_LENGTH` | `1000` | Maximum stored value characters |
| `MEMORY_MAX_PROMPT_ITEMS` | `10` | Maximum selected records per prompt |
| `MEMORY_MAX_PROMPT_CHARACTERS` | `4000` | Maximum key/value prompt characters |
| `MEMORY_USAGE_METADATA_ENABLED` | `true` | Updates last-used time and usage count after success |
| `MEMORY_SUGGESTIONS_ENABLED` | `true` | Permits explicit command proposals; never auto-approves |
| `MEMORY_SUGGESTION_COOLDOWN_MINUTES` | `60` | Reserved bound for user-visible suggestions |

Flyway V5 creates `user_memory` and `message_memory_usage`. PostgreSQL is authoritative. No machine-specific path, secret, embedding model, or cloud setting is required.
