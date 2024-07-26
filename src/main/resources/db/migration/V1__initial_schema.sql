CREATE TABLE IF NOT EXISTS tbl_mtls(
    id SERIAL,
    client_name varchar(50),
    client_code varchar(50),
    doppler_name varchar,
    callback_url text,
    is_mtls_enabled boolean,
    created_at timestamptz,
    updated_at timestamptz
)