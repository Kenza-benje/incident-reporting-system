-- ============================================================================
-- Alertify schema fix script
-- Run this in psql or pgAdmin against the ifrane_sentinel database
-- if you want to keep existing data instead of using create-drop.
--
-- Usage:
--   psql -U postgres -d ifrane_sentinel -f fix_schema.sql
-- ============================================================================

-- Drop tables in reverse FK order so constraints don't block us
DROP TABLE IF EXISTS incident_photos   CASCADE;
DROP TABLE IF EXISTS audit_logs        CASCADE;
DROP TABLE IF EXISTS notifications     CASCADE;
DROP TABLE IF EXISTS status_history    CASCADE;
DROP TABLE IF EXISTS internal_notes    CASCADE;
DROP TABLE IF EXISTS assignments       CASCADE;
DROP TABLE IF EXISTS incidents         CASCADE;
DROP TABLE IF EXISTS users             CASCADE;

-- Let Hibernate recreate everything on next startup.
-- With ddl-auto=create-drop this file is only needed if you want
-- to reset manually without restarting the app.
