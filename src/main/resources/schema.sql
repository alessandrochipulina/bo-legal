-- Active: 1746457224664@@212.56.44.91@31334@dev_bo_legal
CREATE SCHEMA IF NOT EXISTS "core";
DROP VIEW IF EXISTS "core"."document_rates";
DROP TABLE IF EXISTS "core"."document_status";
DROP TABLE IF EXISTS "core"."document_history";
DROP TABLE IF EXISTS "core"."document_type";
DROP TABLE IF EXISTS "core"."document";
DROP TABLE IF EXISTS "core"."rates";
DROP TABLE IF EXISTS "core"."categorie";
DROP TABLE IF EXISTS "core"."document_status_client_description";
-- DROP TABLE IF EXISTS "usuario";

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
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('orange', 'Atendido', 0); -- 4
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('orange', 'En Lugar de Recojo', 0); -- 5
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('yellow', 'En Proceso', 0); -- 6
INSERT INTO "core"."document_status"(color, description, is_deleteable) VALUES('orange', 'Estado Custom 1', 0); -- 7 >

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
  status integer NOT NULL DEFAULT 1,
  status_description varchar NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  modified_at timestamp DEFAULT CURRENT_TIMESTAMP,  
  image_url varchar NULL,
  document_url varchar NULL,
  document_type_id integer,  
  document_type_name varchar NULL,
  document_key varchar NOT NULL,
  document_target_key varchar NULL,
  document_voucher_key varchar NULL,
  user_id varchar NULL,
  user_date varchar NULL,
  user_real_name varchar NULL,
  user_dni varchar NULL, 
  user_local varchar NULL,  
  user_local_name varchar NULL,  
  user_local_type integer NOT NULL DEFAULT 1,  
  portfolio_name varchar NULL,
  user_panel_id integer NOT NULL DEFAULT 0,
  legalization_type integer DEFAULT 1,
  legalization_name varchar NULL,
  price float NULL DEFAULT 0
);

CREATE TABLE "core"."rates" (
  id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (
    START WITH 1
    INCREMENT BY 1
  ),  
  legalization_type integer DEFAULT 1,
  document_type_id integer DEFAULT 1,
  local_type integer DEFAULT 1,
  price float DEFAULT 0,
  user_panel_id integer DEFAULT 0,
  status integer DEFAULT 1,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  modified_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- SERVICIO REGULAR
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 1, 1, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 1, 2, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 1, 3, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 2, 1, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 2, 2, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 2, 3, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 50, 1, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 50, 2, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(1, 50, 3, 10.50);

-- SERVICIO EXPRESS
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 1, 1, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 1, 2, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 1, 3, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 2, 1, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 2, 2, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 2, 3, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 50, 1, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 50, 2, 10.50);
INSERT INTO "core"."rates"(legalization_type, document_type_id, local_type, price) VALUES(2, 50, 3, 10.50);


CREATE TABLE "core"."categorie" (
  id integer PRIMARY KEY  GENERATED ALWAYS AS IDENTITY (
    START WITH 1
    INCREMENT BY 1
  ),  
  categorie_name varchar,
  categorie_id integer,
  categorie_item_name varchar,    
  categorie_item_id integer,  
  user_panel_id integer DEFAULT 0,
  status integer DEFAULT 1,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  modified_at timestamp DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('LEGALIZATION_TYPE', 1, 'REGULAR', 1);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('LEGALIZATION_TYPE', 1, 'EXPRESS', 2);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('DOCUMENT_TYPE_ID', 10, 'SOLICITUD DE LEGALIZACION DE CERTIFICADO - VOUCHER DE PAGO', 1);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('DOCUMENT_TYPE_ID', 10, 'SOLICITUD DE LEGALIZACION DE CONTRATO - VOUCHER DE PAGO', 2);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('DOCUMENT_TYPE_ID', 10, 'SOLICITUD DE LEGALIZACION DE CERTIFICADO', 101);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('DOCUMENT_TYPE_ID', 10, 'SOLICITUD DE LEGALIZACION DE CONTRATO', 102);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('DOCUMENT_TYPE_ID', 10, 'RECTIFICACION DE LUGAR DE RECOJO', 50);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('LOCAL_TYPE', 20, 'LIMA', 1);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('LOCAL_TYPE', 20, 'PROVINCIA', 2);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('LOCAL_TYPE', 20, 'EXTRANJERO', 3);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('REJECT_TYPE', 30, 'No cuenta con el importe correcto', 1);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('REJECT_TYPE', 30, 'Documento ilegible', 2);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('REJECT_TYPE', 30, 'No corresponde al tramite solicitado', 3);
INSERT INTO "core"."categorie" (categorie_name, categorie_id, categorie_item_name, categorie_item_id) VALUES ('REJECT_TYPE', 40, 'Codigo de operacion incorrecto', 4);

CREATE VIEW "core"."document_rates"
AS
SELECT r.id, r.legalization_type as legal_type, a.categorie_item_name as legal_name, 
r.document_type_id as document_type, b.categorie_item_name as document_name, 
r.local_type, c.categorie_item_name as local_name, r.price 
FROM "core"."rates" r 
INNER JOIN "core"."categorie" a ON r.legalization_type = a.categorie_item_id AND a.categorie_name = 'LEGALIZATION_TYPE'
INNER JOIN "core"."categorie" b ON r.document_type_id = b.categorie_item_id AND b.categorie_name = 'DOCUMENT_TYPE_ID'
INNER JOIN "core"."categorie" c ON r.local_type = c.categorie_item_id AND c.categorie_name = 'LOCAL_TYPE'
ORDER BY legalization_type, document_type_id, local_type;

CREATE TABLE "core"."document_status_client_description" (
  id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (
    START WITH 1
    INCREMENT BY 1
  ),
  document_type_id integer DEFAULT 1,
  status integer DEFAULT 1,
  description varchar DEFAULT '',   
  active integer NOT NULL DEFAULT 1
);
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(1, 1, 'PAGO POR VALIDAR');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(1, 3, 'PAGO RECHAZADO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(1, 2, 'PAGO ACEPTADO');

INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(2, 1, 'PAGO POR VALIDAR');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(2, 3, 'PAGO RECHAZADO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(2, 2, 'PAGO ACEPTADO');

INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(50, 1, 'PAGO POR VALIDAR');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(50, 3, 'PAGO RECHAZADO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(50, 2, 'PAGO ACEPTADO');

INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(101, 1, 'SOLICITUD EN PROCESO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(101, 4, 'SOLICITUD ATENDIDA');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(101, 5, 'DEBE ACERCARSE AL LUGAR DE RECOJO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(101, 6, 'SOLICITUD EN PROCESO');

INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(102, 1, 'SOLICITUD EN PROCESO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(102, 4, 'SOLICITUD ATENDIDA');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(102, 5, 'DEBE ACERCARSE AL LUGAR DE RECOJO');
INSERT INTO "core"."document_status_client_description"(document_type_id, status, description) VALUES(102, 6, 'SOLICITUD EN PROCESO');

/*
CREATE TABLE "usuario" (
  id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY (
    START WITH 1
    INCREMENT BY 1
  ),
  nombre varchar NOT NULL UNIQUE,
  correo varchar NOT NULL UNIQUE,
  contrasena varchar NOT NULL,   
  active integer NOT NULL DEFAULT 1
);
*/
