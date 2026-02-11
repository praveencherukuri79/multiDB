-- Check current user permissions
SELECT * FROM fn_my_permissions(NULL, 'DATABASE');

-- Check if user can create tables
SELECT HAS_PERMS_BY_NAME(DB_NAME(), 'DATABASE', 'CREATE TABLE') AS CanCreateTable;

-- Check schema permissions
SELECT HAS_PERMS_BY_NAME('dbo', 'SCHEMA', 'CREATE TABLE') AS CanCreateInDbo;

-- Check if database exists
SELECT name FROM sys.databases WHERE name = 'flowable';

-- Test if you can create a table manually
CREATE TABLE dbo.TEST_TABLE (id INT PRIMARY KEY);
DROP TABLE dbo.TEST_TABLE;


flowable.mssql.create.engine.sql
flowable.mssql.create.history.sql
#flowable.mssql.create.identity.sql

https://github.com/flowable/flowable-engine/tree/flowable-release-7.2.0/modules/flowable-engine/src/main/resources/org/flowable/db/create