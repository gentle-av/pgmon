GRANT ALL ON SCHEMA public TO pgmon_user;
GRANT ALL PRIVILEGES ON DATABASE pgmon TO pgmon_user;
GRANT CREATE ON SCHEMA public TO pgmon_user;

ALTER SCHEMA public OWNER TO pgmon_user;
