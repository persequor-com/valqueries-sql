IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = 'sqlServerSecondary')
    BEGIN;
        CREATE DATABASE sqlServerSecondary;
    END;
GO

