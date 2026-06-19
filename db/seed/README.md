# SkyGrid Demo Seed Data

SkyGrid v1.0.0 uses the idempotent SQL file below as the canonical local seed source:

```text
docker/mysql/init.sql
```

The scripts in `scripts\prepare-demo-data.bat` and `scripts\seed-demo-data.bat` apply that SQL to local MySQL.

This directory records the seed data categories expected by the demo:

- Demo organizations and users.
- 20x20 low-altitude resource grids.
- Altitude levels.
- TimeSlots.
- Route templates.
- Booking workflow records generated during demo execution.

The seed source is centralized to avoid drifting duplicate SQL copies.
