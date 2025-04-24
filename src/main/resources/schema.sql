CREATE SCHEMA IF NOT EXISTS "core";
DROP TABLE IF EXISTS "core"."document_status";
DROP TABLE IF EXISTS "core"."document_history";
DROP TABLE IF EXISTS "core"."document_type";
DROP TABLE IF EXISTS "core"."document";
CREATE TABLE "core"."document_type" (
  id integer PRIMARY KEY,
  description varchar
);
INSERT INTO "core"."document_type"(id, description) VALUES(1, 'Voucher Solicitud de Legalización Certificados');
INSERT INTO "core"."document_type"(id, description) VALUES(2, 'Voucher Solicitud de Legalización Contratos');
INSERT INTO "core"."document_type"(id, description) VALUES(3, 'Voucher Rectificación de Lugar de Recojo');
INSERT INTO "core"."document_type"(id, description) VALUES(101, 'Solicitud de Legalización Certificado');
INSERT INTO "core"."document_type"(id, description) VALUES(102, 'Solicitud de Legalización Contratos');

CREATE SEQUENCE IF NOT EXISTS status_seq START 1;
ALTER SEQUENCE status_seq RESTART WITH 1;

CREATE TABLE "core"."document_status" (
  id integer NOT NULL DEFAULT nextval('status_seq') PRIMARY KEY,
  color varchar DEFAULT 'gray',
  description varchar,
  is_deleteable integer DEFAULT 1,
  active integer NOT NULL DEFAULT 1
);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('green',  'Pendiente', 0);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('blue',   'Aceptado', 0);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('red',    'Rechazado', 0);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('yellow', 'En Proceso', 0);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('orange', 'En Lugar de Recojo', 0);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('orange', 'Atendido', 0);
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('orange', 'Estado Custom 1', 0);

CREATE TABLE "core"."document_history" (
  id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (
    START WITH 1
    INCREMENT BY 1
  ),
  document_key varchar,
  status integer,
  reason_type integer DEFAULT 0,
  reason_text text,
  user_panel_id integer DEFAULT 0,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "core"."document" (
  id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (
    START WITH 1
    INCREMENT BY 1
  ),  
  status integer DEFAULT 1,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  modified_at timestamp DEFAULT CURRENT_TIMESTAMP,  
  image_url varchar,
  document_url varchar,
  document_type_id integer,  
  document_type_name varchar,
  document_key varchar NOT NULL,
  user_id varchar NOT NULL,
  user_date varchar,
  user_real_name varchar,
  user_dni varchar,
  user_local varchar,  
  user_local_type integer DEFAULT 1,  
  portfolio_name varchar,
  user_panel_id integer DEFAULT 0,
  legalization_type integer DEFAULT 1,
  price float NOT NULL DEFAULT 0
);

